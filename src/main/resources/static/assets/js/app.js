;(function (window, document) {
  'use strict';

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

  function initRowLinks(root = document) {
    findAll(root, '[data-row-link]').forEach((row) => {
      if (!(row instanceof HTMLElement)) return;
      if (row.dataset.cmmsRowLinkBound === 'true') return;
      row.dataset.cmmsRowLinkBound = 'true';
      row.addEventListener('click', (event) => {
        const clicked = event.target;
        if (clicked instanceof Element && clicked.closest('a,button,input,select,textarea,label')) {
          return;
        }
        const target = event.currentTarget;
        if (target instanceof HTMLElement) {
          const href = target.getAttribute('data-row-link');
          if (href) window.location.href = href;
        }
      });
    });
  }

  function initConfirmables(root = document) {
    findAll(root, '[data-confirm]').forEach((element) => {
      if (!(element instanceof HTMLElement)) return;
      if (element.dataset.cmmsConfirmBound === 'true') return;
      element.dataset.cmmsConfirmBound = 'true';
      element.addEventListener('click', (event) => {
        const message = element.getAttribute('data-confirm') || '확인하시겠습니까?';
        if (!window.confirm(message)) {
          event.preventDefault();
          event.stopPropagation();
        }
      });
    });
  }

  function initFormValidation(root = document) {
    findAll(root, 'form[data-validate]').forEach((form) => {
      if (!(form instanceof HTMLFormElement)) return;
      if (form.dataset.cmmsValidateBound === 'true') return;
      form.dataset.cmmsValidateBound = 'true';
      form.addEventListener('submit', (event) => {
        if (form.checkValidity()) return;
        event.preventDefault();
        const firstInvalid = form.querySelector(':invalid');
        if (firstInvalid instanceof HTMLElement) {
          firstInvalid.focus();
        }
      });
    });
  }

  function initAttachments(root = document) {
    findAll(root, '[data-attachments]').forEach((section) => {
      if (!(section instanceof HTMLElement)) return;
      if (section.dataset.cmmsAttachmentsBound === 'true') return;
      const fileInput = section.querySelector('#attachments-input');
      const addButton = section.querySelector('[data-attachments-add]');
      const attachmentsList = section.querySelector('.attachments-list');
      if (!fileInput || !addButton || !attachmentsList) return;
      section.dataset.cmmsAttachmentsBound = 'true';
      addButton.addEventListener('click', () => {
        if (fileInput instanceof HTMLInputElement) {
          fileInput.click();
        }
      });
      fileInput.addEventListener('change', (event) => {
        const input = event.target;
        if (!(input instanceof HTMLInputElement) || !input.files) return;
        const files = Array.from(input.files);
        if (files.length === 0) return;
        const emptyMessage = attachmentsList.querySelector('.empty');
        emptyMessage?.remove();
        const docRef = section.ownerDocument || document;
        files.forEach((file) => {
          const listItem = createAttachmentListItem(file, docRef);
          attachmentsList.appendChild(listItem);
        });
        input.value = '';
      });
    });
  }

  function getCookie(name) {
    const cookieString = document.cookie || '';
    const cookies = cookieString.split(';');
    for (let i = 0; i < cookies.length; i += 1) {
      const cookie = cookies[i].trim();
      if (cookie.startsWith(`${name}=`)) {
        return decodeURIComponent(cookie.substring(name.length + 1));
      }
    }
    return '';
  }

  function resolveCsrfToken() {
    const meta = document.querySelector('meta[name="_csrf"]');
    const metaToken = meta?.getAttribute('content')?.trim();
    if (metaToken) return metaToken;
    const candidates = ['XSRF-TOKEN', 'XSRF_TOKEN', '_csrf'];
    for (let i = 0; i < candidates.length; i += 1) {
      const token = getCookie(candidates[i]);
      if (token) return token;
    }
    return '';
  }

  function syncCsrfHiddenFields(root = document) {
    const token = resolveCsrfToken();
    if (!token) return;
    const docRef = root instanceof Document ? root : root?.ownerDocument || document;
    const forms = findAll(root, 'form');
    forms.forEach((form) => {
      if (!(form instanceof HTMLFormElement)) return;
      if (form.hasAttribute('data-skip-csrf')) return;
      let input = form.querySelector('input[name="_csrf"]');
      if (!input) {
        input = docRef.createElement('input');
        input.type = 'hidden';
        input.name = '_csrf';
        form.appendChild(input);
      }
      input.value = token;
    });
    if (docRef && docRef !== document) {
      const meta = document.querySelector('meta[name="_csrf"]');
      if (meta && !meta.getAttribute('content')) {
        meta.setAttribute('content', token);
      }
    }
  }

  function initialize(root = document) {
    syncCsrfHiddenFields(root);
    initRowLinks(root);
    initConfirmables(root);
    initFormValidation(root);
    initAttachments(root);
    if (typeof window.CustomEvent === 'function') {
      const event = new window.CustomEvent('cmms:init', { detail: { root } });
      document.dispatchEvent(event);
    }
  }

  function onReady(callback) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', callback, { once: true });
    } else {
      callback();
    }
  }

  const api = {
    init: initialize,
    initAttachments,
    initConfirmables,
    initFormValidation,
    initRowLinks,
    syncCsrfHiddenFields,
  };

  window.CMMSApp = Object.assign({}, window.CMMSApp || {}, api);
  window.syncCsrfHiddenFields = syncCsrfHiddenFields;

  onReady(() => initialize(document));
})(window, document);
