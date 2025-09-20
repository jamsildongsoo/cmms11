
const CSRF_COOKIE_NAME = 'XSRF-TOKEN';

const CSRF_SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE']);

function readCsrfTokenFromCookie() {
  const cookies = document.cookie ? document.cookie.split('; ') : [];
  for (const entry of cookies) {
    if (entry.startsWith(`${CSRF_COOKIE_NAME}=`)) {
      return decodeURIComponent(entry.split('=').slice(1).join('='));
    }
  }
  return null;
}

function syncCsrfHiddenFields() {
  const token = readCsrfTokenFromCookie();
  if (!token) return;
  const forms = document.querySelectorAll('form');
  forms.forEach((form) => {
    const method = (form.getAttribute('method') || 'get').toUpperCase();
    if (method !== 'POST') return;
    let csrfInput = form.querySelector('input[name="_csrf"]');
    if (!csrfInput) {
      csrfInput = document.createElement('input');
      csrfInput.type = 'hidden';
      csrfInput.name = '_csrf';
      form.appendChild(csrfInput);
    }
    csrfInput.value = token;
  });
}

function shouldAttachCsrfHeader(request) {
  try {
    const url = new URL(request.url);
    const isSameOrigin = url.origin === window.location.origin;
    const method = (request.method || 'GET').toUpperCase();
    return isSameOrigin && !CSRF_SAFE_METHODS.has(method);
  } catch (err) {
    // In case of relative URLs, new URL may throw; default to attaching
    const method = (request.method || 'GET').toUpperCase();
    return !CSRF_SAFE_METHODS.has(method);
  }
}

(function wrapFetchWithCsrf() {
  if (typeof window === 'undefined' || typeof window.fetch !== 'function') {
    return;
  }

  const originalFetch = window.fetch.bind(window);

  window.fetch = function csrfAwareFetch(input, init) {
    const request = new Request(input, init);
    const token = readCsrfTokenFromCookie();

    let finalRequest = request;
    if (token && shouldAttachCsrfHeader(request)) {
      const headers = new Headers(request.headers || {});
      headers.set('X-CSRF-TOKEN', token);
      finalRequest = new Request(request, { headers });
    }

    const responsePromise = originalFetch(finalRequest);
    return responsePromise.finally(() => {
      try {
        syncCsrfHiddenFields();
      } catch (e) {
        // no-op; syncing CSRF fields should not break application flow
      }
    });
  };
})();

document.addEventListener('DOMContentLoaded', () => {
  syncCsrfHiddenFields();

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

  function findAll(root, selector) {
    if (!root) return [];
    const nodes = [];
    if (root instanceof Element && root.matches(selector)) {
      nodes.push(root);
    }
    const scoped = root.querySelectorAll ? root.querySelectorAll(selector) : [];
    return nodes.concat(Array.from(scoped));
  }

  function formatFileSize(bytes) {
    if (!bytes) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    const size = bytes / Math.pow(k, i);
    return `${parseFloat(size.toFixed(2))} ${sizes[i]}`;
  }

  function createAttachmentListItem(file, doc) {
    const documentRef = doc || document;
    const li = documentRef.createElement('li');
    li.className = 'attachment-item';

    const fileName = documentRef.createElement('span');
    fileName.className = 'file-name';
    fileName.textContent = file.name;

    const fileSize = documentRef.createElement('span');
    fileSize.className = 'file-size';
    fileSize.textContent = formatFileSize(file.size);

    const removeButton = documentRef.createElement('button');
    removeButton.type = 'button';
    removeButton.className = 'btn-remove';
    removeButton.textContent = '제거';
    removeButton.setAttribute('aria-label', `${file.name} 파일 제거`);

    removeButton.addEventListener('click', () => {
      const list = li.parentElement;
      li.remove();
      if (!list) return;
      const remainingItems = list.querySelectorAll('.attachment-item');
      if (remainingItems.length === 1) {
        const emptyMessage = documentRef.createElement('li');
        emptyMessage.className = 'empty';
        emptyMessage.textContent = '첨부된 파일이 없습니다.';
        list.appendChild(emptyMessage);
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

window.cmms = window.cmms || {};
window.cmms.refreshCsrfForms = syncCsrfHiddenFields;
