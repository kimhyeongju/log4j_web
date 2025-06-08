let searchCount = 0;
let logMonitorVisible = true;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function () {
  initializePage();
  setupRealTimeSearch();
  startLogSimulation();
});

// 페이지 초기화
function initializePage() {
  // 검색창 포커스
  document.getElementById('mainSearchInput').focus();

  // 검색 알림 표시
  document.getElementById('searchAlert').style.display = 'block';

  // 애니메이션 효과
  animateCards();
}

// 카드 애니메이션
function animateCards() {
  const cards = document.querySelectorAll(
    '.filter-card, .result-card, .vulnerability-banner'
  );
  cards.forEach((card, index) => {
    setTimeout(() => {
      card.style.opacity = '0';
      card.style.transform = 'translateY(20px)';
      card.style.transition = 'all 0.6s ease';
      setTimeout(() => {
        card.style.opacity = '1';
        card.style.transform = 'translateY(0)';
      }, 100);
    }, index * 100);
  });
}

// 🚨 메인 검색 기능 (Log4j 취약점 실습의 핵심)
function performMainSearch() {
  const searchTerm = document.getElementById('mainSearchInput').value;

  if (!searchTerm.trim()) {
    alert('검색어를 입력해주세요.');
    return;
  }

  searchCount++;
  addLogEntry(`[검색 ${searchCount}] 검색어: "${searchTerm}"`);

  // 🚨 Log4j 취약점: API 호출로 서버 로깅 트리거
  fetch(`/api/boards/search?q=${encodeURIComponent(searchTerm)}&type=inventory`)
    .then((response) => response.json())
    .then((data) => {
      updateSearchResults(data, searchTerm);

      // JNDI 패턴 감지
      if (detectJNDIPattern(searchTerm)) {
        showVulnerabilityAlert(searchTerm);
        addLogEntry(`[보안경고] JNDI 패턴 감지: ${searchTerm}`, 'warning');
      }
    })
    .catch((error) => {
      console.error('검색 오류:', error);
      addLogEntry(`[오류] 검색 실패: ${error.message}`, 'error');

      // 임시 결과 표시
      showMockResults(searchTerm);
    });
}

// 🚨 고급 검색 (여러 필터 조합)
function performAdvancedSearch() {
  const filters = {
    category: document.getElementById('categoryFilter').value,
    stock: document.getElementById('stockFilter').value,
    supplier: document.getElementById('supplierFilter').value,
    priceMin: document.getElementById('priceMin').value,
    priceMax: document.getElementById('priceMax').value,
  };

  addLogEntry('[고급검색] 필터 적용 중...');

  // 🚨 Log4j 취약점: 필터 값들을 조합하여 로깅
  const filterString = Object.entries(filters)
    .filter(([key, value]) => value)
    .map(([key, value]) => `${key}:${value}`)
    .join(', ');

  fetch('/api/boards/search/advanced', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(filters),
  })
    .then((response) => response.json())
    .then((data) => {
      addLogEntry(`[고급검색] 필터: ${filterString}`);
      updateSearchResults(data, '고급검색');
    })
    .catch((error) => {
      addLogEntry(`[오류] 고급검색 실패: ${error.message}`, 'error');
      showMockResults('고급검색');
    });
}

// 공격 패턴 삽입
function insertAttackPattern(pattern) {
  document.getElementById('mainSearchInput').value = pattern;

  // 자동 검색 실행
  setTimeout(() => {
    performMainSearch();
  }, 500);

  addLogEntry(`[패턴삽입] ${pattern.substring(0, 50)}...`, 'warning');
}

// 빠른 검색
function quickSearch(term) {
  document.getElementById('mainSearchInput').value = term;
  performMainSearch();
}

// JNDI 패턴 감지
function detectJNDIPattern(input) {
  const jndiPatterns = [
    /\$\{jndi:/i,
    /ldap:\/\//i,
    /rmi:\/\//i,
    /dns:\/\//i,
    /\$\{.*:.*\}/i,
  ];

  return jndiPatterns.some((pattern) => pattern.test(input));
}

// 취약점 경고 표시
function showVulnerabilityAlert(input) {
  const alertHtml = `
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h6 class="fw-bold mb-1">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                        Log4j 취약점 패턴 감지!
                    </h6>
                    <p class="mb-1">입력된 검색어에서 JNDI 패턴이 발견되었습니다.</p>
                    <small>패턴: <code>${input.substring(0, 100)}${
    input.length > 100 ? '...' : ''
  }</code></small>
                </div>
                <div class="col-md-4 text-end">
                    <button class="btn btn-outline-danger btn-sm" onclick="showAttackGuide()">
                        <i class="fas fa-info-circle me-1"></i>상세 정보
                    </button>
                </div>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;

  const container = document.querySelector('.container');
  container.insertAdjacentHTML('afterbegin', alertHtml);
}

// 실시간 검색 설정
function setupRealTimeSearch() {
  const searchInput = document.getElementById('mainSearchInput');
  let timeout;

  searchInput.addEventListener('input', function (e) {
    clearTimeout(timeout);
    const value = e.target.value;

    if (value.length > 2) {
      timeout = setTimeout(() => {
        addLogEntry(`[실시간] 입력: "${value}"`);

        // JNDI 패턴 실시간 감지
        if (detectJNDIPattern(value)) {
          addLogEntry(`[실시간경고] JNDI 패턴 감지중...`, 'warning');
        }
      }, 500);
    }
  });

  // 엔터키 검색
  searchInput.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
      performMainSearch();
    }
  });
}

// 로그 추가
function addLogEntry(message, type = 'info') {
  const logMonitor = document.getElementById('logMonitor');
  const timestamp = new Date().toLocaleTimeString();
  const className =
    type === 'warning'
      ? 'text-warning'
      : type === 'error'
      ? 'text-danger'
      : 'text-success';

  const logEntry = document.createElement('div');
  logEntry.className = className;
  logEntry.innerHTML = `[${timestamp}] ${message}`;

  logMonitor.appendChild(logEntry);
  logMonitor.scrollTop = logMonitor.scrollHeight;

  // 로그 개수 제한
  if (logMonitor.children.length > 50) {
    logMonitor.removeChild(logMonitor.firstChild);
  }
}

// 로그 시뮬레이션
function startLogSimulation() {
  const simulationMessages = [
    'Log4j vulnerability demo system ready',
    'Search monitoring active',
    'JNDI pattern detection enabled',
    'Real-time logging initialized',
  ];

  simulationMessages.forEach((msg, index) => {
    setTimeout(() => {
      addLogEntry(msg);
    }, index * 1000);
  });
}

// 모의 결과 표시
function showMockResults(searchTerm) {
  document.getElementById('resultCount').textContent = '3';
  addLogEntry(`[결과] "${searchTerm}" 검색 완료 - 3건 발견`);

  // 취약점 패턴인 경우 특별 처리
  if (detectJNDIPattern(searchTerm)) {
    addLogEntry('[취약점] JNDI 룩업 시도가 로깅되었습니다', 'warning');
    addLogEntry(
      '[취약점] 실제 환경에서는 외부 서버 연결이 발생합니다',
      'warning'
    );
  }
}

// 검색 결과 업데이트
function updateSearchResults(data, searchTerm) {
  if (data && data.content) {
    document.getElementById('resultCount').textContent =
      data.totalElements || 0;
    addLogEntry(
      `[결과] "${searchTerm}" 검색 완료 - ${data.totalElements || 0}건 발견`
    );
  } else {
    showMockResults(searchTerm);
  }
}

// 모달 및 기타 함수들
function showAttackGuide() {
  new bootstrap.Modal(document.getElementById('attackGuideModal')).show();
}

function toggleLogMonitor() {
  const logMonitor = document.getElementById('logMonitor');
  logMonitorVisible = !logMonitorVisible;
  logMonitor.style.display = logMonitorVisible ? 'block' : 'none';

  addLogEntry(
    logMonitorVisible ? '로그 모니터 활성화' : '로그 모니터 비활성화'
  );
}

function clearLogs() {
  document.getElementById('logMonitor').innerHTML = '';
  addLogEntry('로그 모니터 초기화됨');
}

function resetFilters() {
  document.getElementById('categoryFilter').value = '';
  document.getElementById('stockFilter').value = '';
  document.getElementById('supplierFilter').value = '';
  document.getElementById('priceMin').value = '';
  document.getElementById('priceMax').value = '';
  addLogEntry('[필터] 모든 필터 초기화');
}

function sortResults(type) {
  addLogEntry(`[정렬] ${type} 기준으로 정렬`);
  // 실제 정렬 로직은 여기에 구현
}

function startGuidedTour() {
  bootstrap.Modal.getInstance(
    document.getElementById('attackGuideModal')
  ).hide();

  // 가이드 투어 시작
  alert('가이드 투어를 시작합니다. 첫 번째 공격 패턴을 테스트해보세요!');
  insertAttackPattern('${jndi:ldap://guided-tour.com/step1}');
}

// 실시간 보안 모니터링 시뮬레이션
setInterval(function () {
  if (Math.random() < 0.1) {
    // 10% 확률로 보안 이벤트 시뮬레이션
    const events = [
      'Suspicious search pattern detected',
      'Multiple JNDI attempts from same IP',
      'Potential vulnerability scan detected',
      'Log4j pattern analysis completed',
    ];
    const randomEvent = events[Math.floor(Math.random() * events.length)];
    addLogEntry(`[보안] ${randomEvent}`, 'warning');
  }
}, 15000); // 15초마다 체크
