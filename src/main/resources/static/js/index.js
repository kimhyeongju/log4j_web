// Chart initialization
document.addEventListener('DOMContentLoaded', function () {
  const ctx = document.getElementById('logisticsChart');
  if (ctx) {
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
        datasets: [
          {
            label: '입고',
            data: [12, 19, 3, 5, 2, 3],
            borderColor: 'rgb(75, 192, 192)',
            tension: 0.1,
          },
          {
            label: '출고',
            data: [7, 11, 5, 8, 3, 7],
            borderColor: 'rgb(255, 99, 132)',
            tension: 0.1,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: '오늘의 물류 현황',
          },
        },
      },
    });
  }

  // Page load effects for cards
  const cards = document.querySelectorAll('.dashboard-card');
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

// Search modal function
function showSearchModal() {
  const modalElement = document.getElementById('searchModal');
  if (modalElement) {
    new bootstrap.Modal(modalElement).show();
  }
}

// 🚨 Log4j 취약점 실습: 검색 기능
function performSearch() {
  const keyword = document.getElementById('searchKeyword').value;
  const type = document.getElementById('searchType').value;

  fetch(`/api/boards/search?q=${encodeURIComponent(keyword)}&type=${type}`)
    .then((response) => response.json())
    .then((data) => {
      alert(`검색 완료: "${keyword}" (${data.totalElements || 0}건)`);
      bootstrap.Modal.getInstance(
        document.getElementById('searchModal')
      ).hide();
    })
    .catch((error) => {
      console.error('검색 오류:', error);
      alert('검색 중 오류가 발생했습니다.');
    });
}

// 🚨 Log4j 취약점 실습: 보안 점검
function checkSecurity() {
  fetch('/api/vulnerable/simulate-attack', {
    method: 'POST',
  })
    .then((response) => response.text())
    .then((data) => {
      alert('보안 점검이 완료되었습니다. 시스템 로그를 확인하세요.');
    })
    .catch((error) => {
      console.error('보안 점검 오류:', error);
    });
}
