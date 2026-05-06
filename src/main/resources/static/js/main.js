/* ─── Scroll Reveal ─────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry, i) => {
      if (entry.isIntersecting) {
        setTimeout(() => entry.target.classList.add('revealed'), i * 80);
      }
    });
  }, { threshold: 0.1 });
  document.querySelectorAll('[data-reveal]').forEach(el => observer.observe(el));

  /* ─── Auto-dismiss alerts ─────────────────────────────────── */
  document.querySelectorAll('.alert').forEach(alert => {
    setTimeout(() => {
      alert.style.transition = 'opacity 0.5s ease';
      alert.style.opacity = '0';
      setTimeout(() => alert.remove(), 500);
    }, 4000);
  });

  /* ─── Search/Filter: Job listings ────────────────────────── */
  const jobSearch = document.getElementById('jobSearchInput');
  const jobCards  = document.querySelectorAll('.job-card-item');
  if (jobSearch && jobCards.length) {
    jobSearch.addEventListener('input', filterJobs);
    document.getElementById('locationFilter')?.addEventListener('change', filterJobs);
    document.getElementById('typeFilter')?.addEventListener('change', filterJobs);
  }

  function filterJobs() {
    const kw  = (document.getElementById('jobSearchInput')?.value || '').toLowerCase();
    const loc = (document.getElementById('locationFilter')?.value || '').toLowerCase();
    const typ = (document.getElementById('typeFilter')?.value || '').toLowerCase();
    let visible = 0;
    jobCards.forEach(card => {
      const title   = (card.dataset.title   || '').toLowerCase();
      const company = (card.dataset.company || '').toLowerCase();
      const cardLoc = (card.dataset.location || '').toLowerCase();
      const cardTyp = (card.dataset.type    || '').toLowerCase();
      const matchKw  = !kw  || title.includes(kw)  || company.includes(kw);
      const matchLoc = !loc || cardLoc.includes(loc);
      const matchTyp = !typ || cardTyp.includes(typ);
      const show = matchKw && matchLoc && matchTyp;
      card.style.display = show ? '' : 'none';
      if (show) visible++;
    });
    const counter = document.getElementById('jobCount');
    if (counter) counter.textContent = visible + ' job' + (visible !== 1 ? 's' : '') + ' found';
  }

  /* ─── Navbar scroll shadow ───────────────────────────────── */
  window.addEventListener('scroll', () => {
    const navbar = document.querySelector('.navbar, .landing-navbar');
    if (navbar) {
      navbar.style.boxShadow = window.scrollY > 10
        ? '0 4px 16px rgba(0,0,0,0.12)'
        : '0 1px 3px rgba(0,0,0,0.08)';
    }
  });

  /* ─── Role selector show/hide company field ──────────────── */
  const roleRadios = document.querySelectorAll('input[name="role"]');
  const companyField = document.getElementById('companyField');
  if (roleRadios.length && companyField) {
    roleRadios.forEach(r => {
      r.addEventListener('change', () => {
        companyField.style.display = r.value === 'EMPLOYER' && r.checked ? 'block' : companyField.style.display;
        if (r.value !== 'EMPLOYER') companyField.style.display = 'none';
        if (r.value === 'EMPLOYER' && r.checked) companyField.style.display = 'block';
      });
    });
  }

  /* ─── Confirm delete ─────────────────────────────────────── */
  document.querySelectorAll('[data-confirm]').forEach(btn => {
    btn.addEventListener('click', e => {
      if (!confirm(btn.dataset.confirm)) e.preventDefault();
    });
  });
});
