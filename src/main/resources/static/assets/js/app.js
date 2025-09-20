const CSRF_COOKIE_NAME = 'XSRF-TOKEN';
const CSRF_HEADER_NAME = 'X-CSRF-TOKEN';
const CSRF_FORBIDDEN_ERROR = 'CsrfForbiddenError';

const nativeFetch = window.fetch.bind(window);
let csrfErrorDisplayed = false;

function getCookieValue(name) {
  if (typeof document === 'undefined') return null;
  const cookieString = document.cookie || '';
  const cookies = cookieString.split(';').map((c) => c.trim());
  for (const cookie of cookies) {
    if (!cookie) continue;
    const [cookieName, ...rest] = cookie.split('=');
    if (cookieName === name) {
      return decodeURIComponent(rest.join('='));
    }
  }
  return null;
}

function getCsrfToken() {
  return getCookieValue(CSRF_COOKIE_NAME);
}

function ensureRequestWithHeader(input, init, token) {
  if (!token) {
    return { input, init };
  }

  if (input instanceof Request) {
    const headers = new Headers(init?.headers || input.headers || undefined);
    if (!headers.has(CSRF_HEADER_NAME)) {
      headers.set(CSRF_HEADER_NAME, token);
    }
    const requestInit = { ...init, headers };
    const request = new Request(input, requestInit);
    return { input: request, init: undefined };
  }

  const options = init ? { ...init } : {};
  const headers = new Headers(options.headers || undefined);
  if (!headers.has(CSRF_HEADER_NAME)) {
    headers.set(CSRF_HEADER_NAME, token);
  }
  options.headers = headers;
  return { input, init: options };
}

function syncCsrfForms() {
  if (typeof document === 'undefined') return;
  const token = getCsrfToken();
  const forms = document.querySelectorAll('form');
  forms.forEach((form) => {
    let field = form.querySelector('input[name="_csrf"]');
    if (!field) {
      field = document.createElement('input');
      field.type = 'hidden';
      field.name = '_csrf';
      field.setAttribute('data-csrf-field', 'true');
      form.appendChild(field);
    }
    if (token) {
      field.value = token;
    } else {
      field.value = '';
    }
  });
}

function notifyCsrfError(detail) {
  if (csrfErrorDisplayed) {
    return;
  }
  csrfErrorDisplayed = true;

  console.warn('CSRF protection blocked a request.', detail?.response || detail);

  const loginError = document.getElementById('login_error');
  if (loginError) {
    loginError.textContent = '보안 토큰이 만료되었습니다. 페이지를 새로고침한 후 다시 시도하세요.';
    loginError.setAttribute('role', 'alert');
    loginError.style.display = 'block';
  } else {
    const slot = document.getElementById('layout-slot');
    if (slot) {
      slot.innerHTML = '';
      const wrapper = document.createElement('div');
      wrapper.className = 'notice danger-text';
      wrapper.setAttribute('role', 'alert');
      wrapper.innerHTML = [
        '<div>보안 검증에 실패했습니다. 세션이 만료되었을 수 있습니다.</div>',
        '<div style="margin-top:12px; display:flex; gap:8px; flex-wrap:wrap;">',
        '  <button type="button" class="btn" data-csrf-retry>페이지 새로고침</button>',
        '  <a class="btn" href="/auth/login.html?error=1&msg=' + encodeURIComponent('다시 로그인해 주세요.') + '">다시 로그인</a>',
        '</div>'
      ].join('');
      slot.appendChild(wrapper);
      const retry = wrapper.querySelector('[data-csrf-retry]');
      if (retry) {
        retry.addEventListener('click', () => {
          csrfErrorDisplayed = false;
          window.location.reload();
        });
      }
    } else {
      window.alert('보안 검증에 실패했습니다. 페이지를 새로고침한 후 다시 시도하세요.');
    }
  }

  window.dispatchEvent(new CustomEvent('cmms:csrf-error', { detail }));
}

function createCsrfForbiddenError(response) {
  const error = new Error('Forbidden');
  error.name = CSRF_FORBIDDEN_ERROR;
  error.response = response;
  return error;
}

window.fetch = function csrfAwareFetch(input, init) {
  const token = getCsrfToken();
  let nextInput = input;
  let nextInit = init;
  if (token) {
    const prepared = ensureRequestWithHeader(input, init, token);
    nextInput = prepared.input;
    nextInit = prepared.init;
  }

  return nativeFetch(nextInput, nextInit).then((response) => {
    if (response.status === 403) {
      response.csrfFailure = true;
      notifyCsrfError({ response, request: { input: nextInput, init: nextInit } });
    }
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', syncCsrfForms, { once: true });
    } else {
      syncCsrfForms();
    }
    return response;
  });
};

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', syncCsrfForms, { once: true });
} else {
  syncCsrfForms();
}

document.addEventListener('DOMContentLoaded', () => {
  const tableRows = document.querySelectorAll('[data-row-link]');
  tableRows.forEach((tr) => {
    tr.addEventListener('click', (e) => {
      const clicked = e.target;
      if (clicked instanceof Element && clicked.closest('a,button,input,select,textarea,label')) {
        return; // let the element handle its own click
      }
      const target = e.currentTarget;
      const href = target.getAttribute('data-row-link');
      if (href) window.location.href = href;
    });
  });

  const confirmables = document.querySelectorAll('[data-confirm]');
  confirmables.forEach((el) => {
    el.addEventListener('click', (e) => {
      const msg = el.getAttribute('data-confirm') || '확인하시겠습니까?';
      if (!confirm(msg)) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  });

  const forms = document.querySelectorAll('form[data-validate]');
  forms.forEach((form) => {
    form.addEventListener('submit', (e) => {
      if (!form.checkValidity()) {
        e.preventDefault();
        const firstInvalid = form.querySelector(':invalid');
        firstInvalid?.focus();
      }
    });
  });

  // 파일 첨부 기능
  const attachmentSections = document.querySelectorAll('[data-attachments]');
  attachmentSections.forEach((section) => {
    const fileInput = section.querySelector('#attachments-input');
    const addButton = section.querySelector('[data-attachments-add]');
    const attachmentsList = section.querySelector('.attachments-list');
    
    if (!fileInput || !addButton || !attachmentsList) return;
    
    // 파일 선택 버튼 클릭 시 파일 입력창 열기
    addButton.addEventListener('click', () => {
      fileInput.click();
    });
    
    // 파일 선택 시 목록에 추가
    fileInput.addEventListener('change', (e) => {
      const files = Array.from(e.target.files);
      
      if (files.length === 0) return;
      
      // 빈 메시지 제거
      const emptyMessage = attachmentsList.querySelector('.empty');
      if (emptyMessage) {
        emptyMessage.remove();
      }
      
      // 선택된 파일들을 목록에 추가
      files.forEach((file) => {
        const listItem = createAttachmentListItem(file);
        attachmentsList.appendChild(listItem);
      });
      
      // 파일 입력 초기화 (같은 파일을 다시 선택할 수 있도록)
      fileInput.value = '';
    });
  });
  
  // 첨부 파일 목록 아이템 생성 함수
  function createAttachmentListItem(file) {
    const li = document.createElement('li');
    li.className = 'attachment-item';
    
    const fileName = document.createElement('span');
    fileName.className = 'file-name';
    fileName.textContent = file.name;
    
    const fileSize = document.createElement('span');
    fileSize.className = 'file-size';
    fileSize.textContent = formatFileSize(file.size);
    
    const removeButton = document.createElement('button');
    removeButton.type = 'button';
    removeButton.className = 'btn-remove';
    removeButton.textContent = '제거';
    removeButton.setAttribute('aria-label', `${file.name} 파일 제거`);
    
    // 파일 제거 기능
    removeButton.addEventListener('click', () => {
      li.remove();
      
      // 모든 파일이 제거되면 빈 메시지 표시
      const remainingItems = li.parentElement.querySelectorAll('.attachment-item');
      if (remainingItems.length === 1) { // 현재 제거되는 항목 포함
        const emptyMessage = document.createElement('li');
        emptyMessage.className = 'empty';
        emptyMessage.textContent = '첨부된 파일이 없습니다.';
        li.parentElement.appendChild(emptyMessage);
      }
    });
    
    li.appendChild(fileName);
    li.appendChild(fileSize);
    li.appendChild(removeButton);
    
    return li;
  }
  
  // 파일 크기 포맷팅 함수
  function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
});
