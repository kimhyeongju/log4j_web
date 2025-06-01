// Initialize DataTable
$(document).ready(function () {
  $('#boardTable').DataTable({
    pageLength: 10,
    language: {
      url: '//cdn.datatables.net/plug-ins/1.13.4/i18n/ko.json',
    },
    order: [[7, 'desc']],
  });
});

function showCreateModal() {
  new bootstrap.Modal(document.getElementById('createModal')).show();
}

function showVulnerabilityGuide() {
  new bootstrap.Modal(
    document.getElementById('vulnerabilityGuideModal')
  ).show();
}

function performSearch() {
  const keyword = document.getElementById('searchKeyword').value;
  const type = document.getElementById('searchType').value;

  if (!keyword.trim()) {
    alert('검색어를 입력해주세요.');
    return;
  }

  fetch(
    `/api/boards/search?q=${encodeURIComponent(keyword)}&searchType=${type}`
  )
    .then((response) => response.json())
    .then((data) => {
      updateSearchResults(data);
      if (keyword.includes('${jndi:') || keyword.includes('ldap://')) {
        showVulnerabilityAlert(keyword);
      }
    })
    .catch((error) => {
      console.error('검색 오류:', error);
      alert('검색 중 오류가 발생했습니다.');
    });
}

function quickSearch(keyword) {
  document.getElementById('searchKeyword').value = keyword;
  performSearch();
}

function createPost() {
  const formData = {
    title: document.getElementById('productName').value,
    content: document.getElementById('description').value,
    author: document.getElementById('author').value,
    sku: document.getElementById('sku').value,
    quantity: document.getElementById('quantity').value,
    supplier: document.getElementById('supplier').value,
    priority: document.getElementById('priority').value,
  };

  if (
    !formData.title ||
    !formData.sku ||
    !formData.quantity ||
    !formData.supplier
  ) {
    alert('필수 필드를 모두 입력해주세요.');
    return;
  }

  fetch('/api/boards', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(formData),
  })
    .then((response) => response.text())
    .then(() => {
      alert('입고 등록이 완료되었습니다.');
      bootstrap.Modal.getInstance(
        document.getElementById('createModal')
      ).hide();
      refreshTable();

      if (
        formData.title.includes('${jndi:') ||
        formData.content.includes('${jndi:')
      ) {
        showVulnerabilityAlert('입고 등록 데이터');
      }
    })
    .catch((error) => {
      console.error('등록 오류:', error);
      alert('등록 중 오류가 발생했습니다.');
    });
}

function showVulnerabilityAlert(input) {
  const html = `
    <div class="alert alert-warning alert-dismissible fade show" role="alert">
      <i class="fas fa-exclamation-triangle me-2"></i>
      <strong>보안 경고:</strong> JNDI 패턴이 감지되었습니다: "${input.substring(
        0,
        50
      )}..."
      <br><small>시스템 로그를 확인하여 Log4j 취약점 동작을 확인해보세요.</small>
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>`;
  document.querySelector('.container').insertAdjacentHTML('afterbegin', html);
}

function updateSearchResults(data) {
  console.log('검색 결과:', data);
  alert(`검색 완료: ${data.totalElements || 0}건의 결과를 찾았습니다.`);
}

function viewDetails(id) {
  alert(`상세 정보: ${id}`);
}

function editItem(id) {
  alert(`수정: ${id}`);
}

function refreshTable() {
  $('#boardTable').DataTable().ajax.reload();
  alert('테이블이 새로고침되었습니다.');
}

function exportData() {
  alert('데이터를 내보내는 중...');
}

document
  .getElementById('searchKeyword')
  .addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
      performSearch();
    }
  });

document.addEventListener('DOMContentLoaded', function () {
  const cards = document.querySelectorAll(
    '.board-card, .search-section, .table-container'
  );
  cards.forEach((card, index) => {
    setTimeout(() => {
      card.style.opacity = '0';
      card.style.transform = 'translateY(20px)';
      card.style.transition = 'all 0.5s ease';
      setTimeout(() => {
        card.style.opacity = '1';
        card.style.transform = 'translateY(0)';
      }, 100);
    }, index * 100);
  });
});

setInterval(function () {
  const securityEvents = Math.floor(Math.random() * 30) + 1;
  // 실제로는 UI에 반영하거나 로그할 수 있음
}, 30000);
