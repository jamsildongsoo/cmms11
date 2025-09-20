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
