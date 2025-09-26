
/**
 * CMMS JavaScript 모듈 시스템
 * 
 * 이 파일은 CMMS 애플리케이션의 모든 JavaScript 기능을 통합 관리합니다.
 * 
 * 모듈 구조:
 * - window.cmms.csrf: CSRF 토큰 관리
 * - window.cmms.utils: 유틸리티 함수들
 * - window.cmms.notification: 알림 시스템
 * - window.cmms.navigation: SPA 네비게이션
 * - window.cmms.fileUpload: 파일 업로드 위젯
 * - window.cmms.user: 사용자 정보 관리
 * 
 * 사용법:
 * - window.cmms.notification.success('성공 메시지');
 * - window.cmms.navigation.navigate('/plant/list');
 * - window.cmms.fileUpload.init(container);
 */

// =============================================================================
// CSRF 토큰 관리 관련 상수 및 함수
// =============================================================================

const CSRF_COOKIE_NAME = 'XSRF-TOKEN';

const CSRF_SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE']);

/**
 * 쿠키에서 CSRF 토큰을 읽어오는 함수
 * @returns {string|null} CSRF 토큰 또는 null
 */
function readCsrfTokenFromCookie() {
  const cookies = document.cookie ? document.cookie.split('; ') : [];
  for (const entry of cookies) {
    if (entry.startsWith(`${CSRF_COOKIE_NAME}=`)) {
      return decodeURIComponent(entry.split('=').slice(1).join('='));
    }
  }
  return null;
}

/**
 * 모든 폼의 CSRF hidden 필드를 동기화하는 함수
 * POST 메서드를 사용하는 폼에 CSRF 토큰을 자동으로 추가
 */
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

/**
 * 요청에 CSRF 헤더를 첨부해야 하는지 판단하는 함수
 * @param {Request} request - Fetch API Request 객체
 * @returns {boolean} CSRF 헤더 첨부 여부
 */
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

// =============================================================================
// Fetch API 래핑 - CSRF 토큰 자동 첨부
// =============================================================================

/**
 * Fetch API를 래핑하여 CSRF 토큰을 자동으로 첨부하는 IIFE
 * 모든 fetch 요청에 X-CSRF-TOKEN 헤더를 자동으로 추가
 */
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
        window.cmms.csrf.refreshForms();
      } catch (e) {
        // no-op; syncing CSRF fields should not break application flow
      }
    });
  };
})();

// =============================================================================
// DOM 이벤트 리스너 및 UI 상호작용 처리
// =============================================================================

/**
 * DOM 로드 완료 후 실행되는 초기화 함수
 * 테이블 행 클릭, 확인 다이얼로그, 파일 첨부 등의 이벤트 리스너 설정
 */
document.addEventListener('DOMContentLoaded', () => {
  window.cmms.csrf.refreshForms();

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

  
});

// =============================================================================
// 유틸리티 함수들 (전역 스코프)
// =============================================================================

/**
 * 루트 요소에서 특정 셀렉터와 일치하는 모든 요소를 찾는 함수
 * @param {Element} root - 검색할 루트 요소
 * @param {string} selector - CSS 셀렉터
 * @returns {Element[]} 찾은 요소들의 배열
 */
  function findAll(root, selector) {
    if (!root) return [];
    const nodes = [];
    if (root instanceof Element && root.matches(selector)) {
      nodes.push(root);
    }
    const scoped = root.querySelectorAll ? root.querySelectorAll(selector) : [];
    return nodes.concat(Array.from(scoped));
  }

/**
 * 바이트 크기를 사람이 읽기 쉬운 형태로 포맷팅하는 함수
 * @param {number} bytes - 바이트 크기
 * @returns {string} 포맷팅된 파일 크기 문자열
 */
  function formatFileSize(bytes) {
    if (!bytes) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    const size = bytes / Math.pow(k, i);
    return `${parseFloat(size.toFixed(2))} ${sizes[i]}`;
  }

/**
 * 파일 첨부 목록 항목을 생성하는 함수
 * @param {File} file - 첨부할 파일 객체
 * @param {Document} doc - 문서 객체 (기본값: document)
 * @returns {HTMLLIElement} 생성된 리스트 항목 요소
 */
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
  
// =============================================================================
// CMMS 전역 네임스페이스 및 SPA 기능
// =============================================================================

window.cmms = window.cmms || {};

// =============================================================================
// CSRF 관리 모듈
// =============================================================================
window.cmms.csrf = {
  refreshForms: syncCsrfHiddenFields,
  readToken: readCsrfTokenFromCookie,
  shouldAttachHeader: shouldAttachCsrfHeader,
  toCsrfError: function(response) {
    if (typeof createCsrfForbiddenError === 'function') {
      return createCsrfForbiddenError(response);
    }
    const error = new Error('Forbidden');
    error.name = 'CsrfForbiddenError';
    error.response = response;
    return error;
  },
  handleError: function(response) {
    if (response.status === 403) {
      window.cmms.notification.error('세션이 만료되었습니다. 페이지를 새로고침해주세요.');
      setTimeout(() => {
        window.location.reload();
      }, 2000);
      return true;
    }
    return false;
  }
};

// =============================================================================
// 유틸리티 모듈
// =============================================================================
window.cmms.utils = {
  formatFileSize: formatFileSize,
  findAll: findAll,
  createAttachmentListItem: createAttachmentListItem
};

// =============================================================================
// 알림 시스템 모듈
// =============================================================================
window.cmms.notification = {
  show: function(message, type = 'info', duration = 3000) {
    // 기존 알림 제거
    const existing = document.querySelector('.cmms-notification');
    if (existing) {
      existing.remove();
    }

    // 새 알림 생성
    const notification = document.createElement('div');
    notification.className = `cmms-notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      max-width: 400px;
      padding: 12px 16px;
      border-radius: 4px;
      color: white;
      font-weight: 500;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    `;
    
    // 타입별 스타일
    if (type === 'success') {
      notification.style.backgroundColor = '#10b981';
    } else if (type === 'error') {
      notification.style.backgroundColor = '#ef4444';
    } else if (type === 'warning') {
      notification.style.backgroundColor = '#f59e0b';
    } else {
      notification.style.backgroundColor = '#3b82f6';
    }
    
    document.body.appendChild(notification);

    // 자동 제거
    setTimeout(() => {
      if (notification.parentNode) {
        notification.remove();
      }
    }, duration);
  },
  
  success: function(message) {
    this.show(message, 'success');
  },
  
  error: function(message) {
    this.show(message, 'error');
  },
  
  warning: function(message) {
    this.show(message, 'warning');
  }
};

// =============================================================================
// 네비게이션 모듈 (SPA 기능)
// =============================================================================
window.cmms.navigation = {
  slot: null,
  currentContentUrl: '../plant/list.html',

  /**
   * Thymeleaf 스타일 링크와 상대/절대 URL을 실제 URL로 변환하는 함수
   * @param {string} href - 변환할 URL
   * @param {string} basePath - 기준 경로
   * @returns {string} 변환된 URL
   */
  resolveUrl: function resolveUrl(href, basePath) {
    try {
      // Handle unprocessed Thymeleaf-style links like '@{/domain/company/list}'
      if (href && href.startsWith('@{') && href.endsWith('}')) {
        href = href.slice(2, -1); // strip '@{' and '}'
      }
      // Absolute or external URLs: return as-is
      if (href.startsWith('http://') || href.startsWith('https://') || href.startsWith('/')) {
        return href;
      }

      // Treat non-HTML app routes (e.g., domain/company/list, api/...) as root-relative
      const isHtmlFile = /\.html($|[?#])/.test(href);
      if (!isHtmlFile) {
        return '/' + href.replace(/^\/+/, '');
      }

      // For HTML partials, resolve relative to the current content directory
      const baseDir = (() => {
        if (!basePath) return '/';
        const idx = basePath.lastIndexOf('/');
        if (idx <= 0) return '/';
        return basePath.slice(0, idx + 1);
      })();
      const u = new URL(href, window.location.origin + baseDir);
      return u.pathname + u.search;
    } catch (_) {
      return href;
    }
  },

  /**
   * 브라우저 히스토리에 상태를 저장하고 URL을 업데이트하는 함수
   * @param {string} contentUrl - 콘텐츠 URL
   * @param {boolean} push - 히스토리 푸시 여부
   */
  setState: function setState(contentUrl, push) {
    const url = '/layout/defaultLayout.html?content=' + encodeURIComponent(contentUrl);
    const state = { content: contentUrl };
    if (push) history.pushState(state, '', url);
    else history.replaceState(state, '', url);
  },

  /**
   * 현재 페이지에 해당하는 사이드바 메뉴 항목을 활성화하는 함수
   * @param {string} contentUrl - 콘텐츠 URL
   */
  setActive: function setActive(contentUrl) {
    try {
      const links = document.querySelectorAll('.sidebar .menu-item');
      const targetPath = new URL(contentUrl, window.location.origin).pathname;
      links.forEach((a) => {
        a.classList.remove('active');
        const href = a.getAttribute('href') || '';
        if (!href.startsWith('/layout/')) return;
        const u = new URL(href, window.location.origin);
        const c = u.searchParams.get('content') || '';
        if (!c) return;
        if (new URL(c, window.location.origin).pathname === targetPath) {
          a.classList.add('active');
          const grp = a.closest('.menu-group');
          if (grp && !grp.classList.contains('open')) {
            grp.classList.add('open');
            const btn = grp.querySelector('.menu-title');
            if (btn) btn.setAttribute('aria-expanded', 'true');
          }
        }
      });
    } catch (_) { /* noop */ }
  },

  /**
   * AJAX로 콘텐츠를 가져와서 슬롯에 삽입하고 폼 제출을 인터셉트하는 핵심 함수
   * @param {string} contentUrl - 콘텐츠 URL
   * @param {Object} opts - 옵션 객체
   */
  loadContent: function loadContent(contentUrl, opts = { push: false }) {
    this.currentContentUrl = contentUrl;
    if (opts.push === true) this.setState(contentUrl, true);
    
    // URL 유효성 검사
    if (!contentUrl || contentUrl.trim() === '') {
      console.warn('Empty content URL, redirecting to default');
      this.navigate('../plant/list.html');
      return;
    }
    
    // 잘못된 URL 패턴 검사
    if (contentUrl.includes('..') && contentUrl.split('..').length > 2) {
      console.warn('Invalid URL pattern detected:', contentUrl);
      this.slot.innerHTML = `
        <div class="notice danger">
          <h3>잘못된 URL입니다</h3>
          <p>보안상의 이유로 해당 URL에 접근할 수 없습니다.</p>
          <a class="btn primary" href="/domain/company/list">목록으로 이동</a>
        </div>
      `;
      return;
    }
    
    fetch(contentUrl, { credentials: 'same-origin' })
      .then(r => {
        if (r.status === 403) {
          throw window.cmms.csrf.toCsrfError(r);
        }
        return r.text();
      })
      .then(html => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const main = doc.querySelector('main');
        this.slot.innerHTML = main ? main.innerHTML : (doc.body ? doc.body.innerHTML : html);
        
        // Try to sync CSRF hidden fields if available
        if (typeof window.cmms?.csrf?.refreshForms === 'function') {
          try { window.cmms.csrf.refreshForms(); } catch (_) {}
        }

        // Intercept SPA-friendly form submissions inside slot
        const forms = this.slot.querySelectorAll('form[data-redirect]');
        forms.forEach((form) => {
          if (form.__cmmsHandled) return;
          form.__cmmsHandled = true;
          form.addEventListener('submit', (e) => {
            try {
              e.preventDefault();
              const action = form.getAttribute('action') || '';
              const method = (form.getAttribute('method') || 'post').toUpperCase();
              const redirectTo = form.getAttribute('data-redirect') || this.currentContentUrl;
              const formData = new FormData(form);
              fetch(action, {
                method,
                body: formData,
                credentials: 'same-origin'
              }).then((res) => {
                if (res.status === 403) throw window.cmms.csrf.toCsrfError(res);
                if (!res.ok) throw new Error('Submit failed: ' + res.status);
                this.navigate(redirectTo);
              }).catch((err) => {
                console.error(err);
                const notice = document.createElement('div');
                notice.className = 'notice danger';
                notice.textContent = '저장에 실패했습니다. 잠시 후 다시 시도하세요.';
                form.prepend(notice);
              });
            } catch (err) {
              console.error(err);
            }
          });
        });
        
        this.setActive(this.currentContentUrl);
        const title = doc.querySelector('title');
        if (title && title.textContent) document.title = title.textContent + ' · CMMS';
      })
      .catch(err => {
        if (err && err.name === 'CsrfForbiddenError') {
          return;
        }
        console.error('Content load error:', err);
        
        // URL이 잘못된 경우 기본 페이지로 리다이렉트
        if (err.message.includes('404') || err.message.includes('Not Found')) {
          this.slot.innerHTML = `
            <div class="notice danger">
              <h3>페이지를 찾을 수 없습니다</h3>
              <p>요청하신 페이지가 존재하지 않습니다.</p>
              <a class="btn primary" href="/domain/company/list">홈으로 이동</a>
            </div>
          `;
          // 3초 후 자동으로 기본 페이지로 이동
          setTimeout(() => {
            this.navigate('../plant/list.html');
          }, 3000);
        } else if (err.message.includes('403') || err.message.includes('Forbidden')) {
          this.slot.innerHTML = `
            <div class="notice danger">
              <h3>접근 권한이 없습니다</h3>
              <p>이 페이지에 접근할 권한이 없습니다.</p>
              <a class="btn primary" href="/domain/company/list">목록으로 이동</a>
            </div>
          `;
        } else {
          this.slot.innerHTML = `
            <div class="notice danger">
              <h3>콘텐츠를 불러오지 못했습니다</h3>
              <p>네트워크 오류 또는 서버 문제가 발생했습니다.</p>
              <button class="btn primary" onclick="location.reload()">새로고침</button>
              <a class="btn" href="/domain/company/list">목록으로 이동</a>
            </div>
          `;
        }
      });
  },

  /**
   * URL을 해석하고 적절한 콘텐츠로 네비게이션하는 함수
   * @param {string} targetHref - 대상 URL
   */
  navigate: function navigate(targetHref) {
    const contentUrl = targetHref.startsWith('/layout/')
      ? (new URLSearchParams(new URL(targetHref, window.location.origin).search).get('content') || '../plant/list.html')
      : this.resolveUrl(targetHref, this.currentContentUrl);
    try { console.debug('[cmms-nav]', { targetHref, base: this.currentContentUrl, contentUrl }); } catch (_) {}
    this.setState(contentUrl, true);
    this.loadContent(contentUrl, { push: false });
  },

  /**
   * HTML 부분을 가져와서 슬롯 요소에 주입하는 SPA 기능
   * [data-slot-root] 요소를 우선적으로 찾고, 없으면 <main> 또는 <body>를 사용
   * 
   * @param {string} url - 가져올 HTML의 URL
   * @param {Element} slotEl - 콘텐츠를 주입할 슬롯 요소
   * @param {Object} opts - 옵션 객체
   * @param {boolean} opts.pushState - 브라우저 히스토리 푸시 여부
   * @param {Function} opts.onAfterInject - 주입 후 콜백 함수
   * @returns {Promise<Object>} 문서 객체와 루트 요소를 포함한 객체
   */
  fetchAndInject: async function fetchAndInject(url, slotEl, opts) {
  const options = Object.assign({ pushState: false, onAfterInject: null }, opts);
  if (!slotEl) throw new Error('fetchAndInject: slot element is required');
  const res = await fetch(url, { credentials: 'same-origin' });
  if (!res.ok) throw new Error('Failed to fetch: ' + res.status);
  const html = await res.text();

  const parser = new DOMParser();
  const doc = parser.parseFromString(html, 'text/html');

  // Prefer explicit slot root, then a main, then body.
  const root =
    doc.querySelector('[data-slot-root]') ||
    doc.querySelector('main') ||
    doc.body;

  // 콘텐츠 주입
  slotEl.innerHTML = root.innerHTML;

  // 문서 제목 동기화 (제공된 경우)
  const title = doc.querySelector('title');
  if (title && title.textContent) {
    try { document.title = title.textContent; } catch (_) {}
  }

  // SPA 스타일 제출을 위한 [data-validate] 폼 연결
  const forms = slotEl.querySelectorAll('form[data-validate]');
  forms.forEach((form) => {
    if (form.__cmmsBound) return;
    form.__cmmsBound = true;
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const fd = new FormData(form);
      const action = form.getAttribute('action') || window.location.href;
      const method = (form.getAttribute('method') || 'post').toUpperCase();
      const redirectTo = form.getAttribute('data-redirect');
      fetch(action, {
        method,
        credentials: 'same-origin',
        body: fd,
      })
        .then((res) => {
          if (!res.ok) throw new Error('Submit failed: ' + res.status);
          if (redirectTo) {
            window.location.href = redirectTo;
          } else if (typeof options.onAfterInject === 'function') {
            options.onAfterInject({ form, res });
          }
        })
        .catch((err) => {
          console.error(err);
          const notice = document.createElement('div');
          notice.className = 'notice danger';
          notice.textContent = '요청이 실패했습니다. 잠시 후 다시 시도하세요.';
          form.prepend(notice);
        });
    });
  });

  // CSRF hidden 필드 새로고침 보장
  try { window.cmms.csrf.refreshForms(); } catch (_) {}

  return { doc, root };
  },

  /**
   * 사용자 정보 로드 함수 (Thymeleaf로 서버에서 주입되므로 더 이상 필요하지 않음)
   * @deprecated Thymeleaf 템플릿에서 사용자 정보를 직접 주입하므로 사용하지 않음
   */
  loadUserInfo: function loadUserInfo() {
    // Thymeleaf 템플릿에서 사용자 정보를 직접 주입하므로 
    // JavaScript에서 별도로 로드할 필요가 없음
    console.log('사용자 정보는 Thymeleaf 템플릿에서 직접 주입됩니다.');
  },

  /**
   * 삭제 핸들러를 바인딩하는 함수
   */
  bindDeleteHandler: function bindDeleteHandler() {
    const slot = this.slot;
    if (!slot) return;
    
    slot.addEventListener('click', (e) => {
      const btn = e.target.closest('[data-delete-url]');
      if (!btn) return;
      e.preventDefault();
      const url = btn.getAttribute('data-delete-url');
      const redirectTo = btn.getAttribute('data-redirect') || this.currentContentUrl;
      const confirmMsg = btn.getAttribute('data-confirm');
      if (confirmMsg && !confirm(confirmMsg)) return;
      fetch(url, { method: 'POST', credentials: 'same-origin' })
        .then((res) => {
          if (res.status === 403) throw window.cmms.csrf.toCsrfError(res);
          if (!res.ok) throw new Error('Delete failed: ' + res.status);
          this.navigate(redirectTo);
        })
        .catch((err) => {
          console.error(err);
          window.cmms.notification.error('삭제에 실패했습니다. 잠시 후 다시 시도하세요.');
        });
    }, { capture: true });
  },

  /**
   * 사이드바 토글 기능을 초기화하는 함수
   */
  initSidebarToggle: function initSidebarToggle() {
    document.querySelectorAll('.sidebar .menu-title').forEach((btn) => {
      btn.addEventListener('click', () => {
        const grp = btn.closest('.menu-group');
        if (!grp) return;
        const willOpen = !grp.classList.contains('open');
        grp.classList.toggle('open', willOpen);
        btn.setAttribute('aria-expanded', willOpen ? 'true' : 'false');
      });
    });
  },

  /**
   * 네비게이션 시스템을 초기화하는 함수
   */
  init: function init() {
    // 슬롯 요소 찾기
    this.slot = document.getElementById('layout-slot');
    if (!this.slot) {
      console.warn('layout-slot element not found');
      return;
    }

    // 클릭 이벤트 리스너 등록
    document.addEventListener('click', (e) => {
      const anchor = e.target.closest('a[href]');
      if (anchor && anchor.getAttribute('href')) {
        const href = anchor.getAttribute('href');
        // Bypass SPA for external, auth, or explicit hard-nav links
        if (
          href.startsWith('http') ||
          href.startsWith('mailto:') ||
          href.startsWith('#') ||
          href.startsWith('/api/auth/logout') ||
          href.startsWith('/auth/') ||
          anchor.hasAttribute('data-hard-nav') ||
          anchor.target === '_blank'
        ) {
          return; // let browser handle
        }
        e.preventDefault();
        this.navigate(href);
        return;
      }
      const row = e.target.closest('[data-row-link]');
      if (row) {
        const link = row.getAttribute('data-row-link');
        if (link) {
          e.preventDefault();
          this.navigate(link);
        }
      }
    }, { capture: true });

    // 뒤로/앞으로 버튼 처리
    window.addEventListener('popstate', (e) => {
      const content = e.state?.content || new URLSearchParams(window.location.search).get('content') || '../plant/list.html';
      try {
        this.loadContent(content, { push: false });
      } catch (err) {
        console.error('Navigation error:', err);
        // 에러 발생 시 기본 페이지로 이동
        this.navigate('../plant/list.html');
      }
    });

    // 초기 로드 - Thymeleaf에서 전달된 콘텐츠 URL 사용
    const initialContent = window.initialContent || new URLSearchParams(window.location.search).get('content') || '../plant/list.html';
    this.setState(initialContent, false);
    this.loadContent(initialContent, { push: false });

    // 삭제 핸들러 바인딩
    this.bindDeleteHandler();

    // 사용자 정보는 Thymeleaf 템플릿에서 직접 주입되므로 로드할 필요 없음
    // this.loadUserInfo();

    // 사이드바 토글 초기화
    this.initSidebarToggle();
  }
};

// =============================================================================
// 사용자 정보 관리 모듈
// =============================================================================
window.cmms.user = {
  /**
   * 현재 사용자 정보에서 회사코드를 안전하게 추출하는 함수
   * @returns {string} 회사코드
   */
  getCurrentCompanyId: function() {
    // 실제로는 백엔드에서 인증된 사용자 정보를 통해 추출
    // 프론트엔드에서는 회사코드를 직접 조작할 수 없음
    
    // 예시: 사용자명이 "C0001:admin" 형태인 경우
    const username = "C0001:admin"; // 실제로는 인증 정보에서 가져옴
    
    if (username && username.includes(":")) {
      const [companyId, memberId] = username.split(":", 2);
      return companyId; // "C0001"
    }
    
    return "C0001"; // 기본값
  },

  /**
   * 파일 업로드 시 회사코드는 백엔드에서 자동으로 결정됨
   * @param {FileList} files - 업로드할 파일들
   * @param {Object} options - 업로드 옵션
   * @returns {Promise} 업로드 응답
   */
  uploadFiles: async function(files, options = {}) {
    // ❌ 잘못된 방법: 프론트엔드에서 회사코드를 전달
    // const companyId = "C0002"; // 악의적 사용자가 조작 가능
    
    // ✅ 올바른 방법: 백엔드에서 사용자 정보로부터 추출
    const response = await fetch('/api/files/init', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        // companyId를 전달하지 않음 - 백엔드에서 자동 결정
        groupId: options.groupId,
        refEntity: options.refEntity,
        refId: options.refId,
        fileNames: Array.from(files).map(f => f.name)
      })
    });
    
    // 백엔드에서 인증된 사용자의 회사코드를 사용하여 S3 경로 생성
    // 예: C0001/plant/F250119001/파일명.jpg
    return response;
  },

  /**
   * 보안 강화 정보 출력
   */
  showSecurityInfo: function() {
    console.log(`
보안 강화 전:
- 프론트엔드에서 회사코드 전달: companyId: "C0002" (조작 가능)
- S3 경로: C0002/plant/F250119001/파일명.jpg (다른 회사 데이터 접근)

보안 강화 후:
- 백엔드에서 사용자 정보로 회사코드 추출: "C0001:admin" -> "C0001"
- S3 경로: C0001/plant/F250119001/파일명.jpg (본인 회사 데이터만 접근)
    `);
  }
};
