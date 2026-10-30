/* ============================================================
   SPIS application shell: hash router + role-based navigation
   All authenticated users land here after login.
   ============================================================ */

/* ---------- module registry ---------- */
const MODULES = {
  dashboard:   { title: 'Dashboard',        sub: 'Institution overview at a glance',      view: 'js/views/dashboard.js',   roles: ['ADMIN', 'FACULTY', 'STUDENT'] },
  students:    { title: 'Students',         sub: 'Search, add, edit and manage students', view: 'js/views/students.js',    roles: ['ADMIN', 'FACULTY'] },
  student:     { title: 'Student Profile',  sub: 'Complete academic profile',             view: 'js/views/student-detail.js', roles: ['ADMIN', 'FACULTY'] },
  faculty:     { title: 'Faculty',          sub: 'Manage faculty members',                view: 'js/views/faculty.js',     roles: ['ADMIN'] },
  departments: { title: 'Departments',      sub: 'Manage academic departments',           view: 'js/views/departments.js', roles: ['ADMIN'] },
  users:       { title: 'Users',            sub: 'Accounts, roles and student linking',   view: 'js/views/users.js',       roles: ['ADMIN'] },
  subjects:    { title: 'Subjects',         sub: 'Subjects, faculty and credits',         view: 'js/views/subjects.js',    roles: ['ADMIN', 'FACULTY'] },
  exams:       { title: 'Exams',            sub: 'Exam schedule and total marks',         view: 'js/views/exams.js',       roles: ['ADMIN', 'FACULTY'] },
  enrollments: { title: 'Enrollments',      sub: 'Who studies what, and when',            view: 'js/views/enrollments.js', roles: ['ADMIN', 'FACULTY'] },
  marks:       { title: 'Marks',            sub: 'Enter and manage marks',                view: 'js/views/marks.js',       roles: ['ADMIN', 'FACULTY'] },
  attendance:  { title: 'Attendance',       sub: 'Take attendance and review history',    view: 'js/views/attendance.js',  roles: ['ADMIN', 'FACULTY'] },
  analytics:   { title: 'Analytics',        sub: 'Performance and attendance analytics',  view: 'js/views/analytics.js',   roles: ['ADMIN', 'FACULTY'] },
  rankings:    { title: 'Rankings',         sub: 'Students, subjects, departments, attendance', view: 'js/views/rankings.js', roles: ['ADMIN', 'FACULTY', 'STUDENT'] },
  reports:     { title: 'Reports',          sub: 'Generate and export reports',           view: 'js/views/reports.js',     roles: ['ADMIN', 'FACULTY'] },
  ai:          { title: 'AI Intelligence',  sub: 'ML-powered predictions and risk alerts', view: 'js/views/ai.js',         roles: ['ADMIN', 'FACULTY'] },
  notifications: { title: 'Notifications',  sub: 'Alerts generated from live data',       view: 'js/views/notifications.js', roles: ['ADMIN', 'FACULTY', 'STUDENT'] },
  profile:     { title: 'Profile',          sub: 'Your account details',                  view: 'js/views/profile.js',     roles: ['ADMIN', 'FACULTY', 'STUDENT'] },
  /* student portal */
  myacademics: { title: 'My Academics',     sub: 'Marks, attendance and subjects',        view: 'js/views/my-academics.js', roles: ['STUDENT'] },
  myperformance: { title: 'Performance',    sub: 'Your performance breakdown',            view: 'js/views/my-performance.js', roles: ['STUDENT'] },
  myai:        { title: 'My AI Insights',   sub: 'Your personal AI predictions',          view: 'js/views/my-ai.js',       roles: ['STUDENT'] }
};

const NAV = {
  ADMIN: [
    { link: '#dashboard',  icon: '📊', label: 'Dashboard' },
    { link: '#students',   icon: '🎓', label: 'Students' },
    { link: '#faculty',    icon: '👨‍🏫', label: 'Faculty' },
    { link: '#departments', icon: '🏛️', label: 'Departments' },
    { link: '#subjects',   icon: '📚', label: 'Subjects' },
    { link: '#users',      icon: '👤', label: 'Users' },
    { link: '#analytics',  icon: '📈', label: 'Analytics' },
    { link: '#rankings',   icon: '🏆', label: 'Rankings' },
    { link: '#reports',    icon: '🧾', label: 'Reports' },
    { link: '#ai',         icon: '🤖', label: 'AI Intelligence' },
    { link: '#notifications', icon: '🔔', label: 'Notifications' },
    { link: '#profile',    icon: '⚙️', label: 'Profile' }
  ],
  FACULTY: [
    { link: '#dashboard',  icon: '📊', label: 'Dashboard' },
    { link: '#students',   icon: '🎓', label: 'Students' },
    {
      icon: '📚', label: 'Academic', sub: [
        { link: '#marks',       label: 'Marks' },
        { link: '#attendance',  label: 'Attendance' },
        { link: '#subjects',    label: 'Subjects' },
        { link: '#exams',       label: 'Exams' },
        { link: '#enrollments', label: 'Enrollments' }
      ]
    },
    { link: '#analytics',  icon: '📈', label: 'Analytics' },
    { link: '#rankings',   icon: '🏆', label: 'Rankings' },
    { link: '#ai',         icon: '🤖', label: 'AI Intelligence' },
    { link: '#reports',    icon: '🧾', label: 'Reports' },
    { link: '#notifications', icon: '🔔', label: 'Notifications' },
    { link: '#profile',    icon: '⚙️', label: 'Profile' }
  ],
  STUDENT: [
    { link: '#dashboard',     icon: '📊', label: 'Dashboard' },
    { link: '#myacademics',   icon: '📚', label: 'My Academics' },
    { link: '#myperformance', icon: '📈', label: 'Performance' },
    { link: '#myai',          icon: '🤖', label: 'My AI Insights' },
    { link: '#rankings',      icon: '🏆', label: 'Rankings' },
    { link: '#notifications', icon: '🔔', label: 'Notifications' },
    { link: '#profile',       icon: '⚙️', label: 'Profile' }
  ]
};

/* ---------- navigation rendering ---------- */
function buildNav() {
  const role = getRole();
  const items = NAV[role] || [];
  const nav = $('#sideNav');
  nav.innerHTML = items.map(item => {
    if (item.sub) {
      return '<div class="nav-section"></div>' +
        '<a href="javascript:void(0)" class="group-toggle" data-group="' + esc(item.label) + '">' +
        '<span class="icon">' + item.icon + '</span>' + esc(item.label) + '<span class="chev">▶</span></a>' +
        '<div class="subnav" data-group-body="' + esc(item.label) + '">' +
        item.sub.map(s => '<a href="' + s.link + '" data-route="' + s.link.slice(1) + '">' + esc(s.label) + '</a>').join('') +
        '</div>';
    }
    return '<a href="' + item.link + '" data-route="' + item.link.slice(1) + '">' +
      '<span class="icon">' + item.icon + '</span>' + esc(item.label) + '</a>';
  }).join('');

  $all('.group-toggle', nav).forEach(a => {
    a.addEventListener('click', () => {
      const g = a.dataset.group;
      $('[data-group-body="' + g + '"]', nav).classList.toggle('open');
    });
  });

  $('#logoutBtn').onclick = logout;
  $('#menuBtn').onclick = () => $('#sidebar').classList.toggle('open');

  /* notification bell opens the panel with live alerts */
  $('#notifBtn').onclick = () => { openModal('notifPanel'); renderNotifPanel(); };
  if (role !== 'STUDENT') prefetchNotifCount();

  /* pill element exists only inside views that requested it */
  if ($('#aiHealthPill')) refreshAIHealth();
}

/* ---------- router ---------- */
let currentRoute = null;

async function route() {
  if (!requireAuth()) return;
  const role = getRole();
  let name = (location.hash || '#dashboard').slice(1);
  if (name === 'profile-student') name = 'profile';

  let mod = MODULES[name];
  if (!mod || !mod.roles.includes(role)) {
    // fallback: first allowed module
    name = Object.keys(MODULES).find(k => MODULES[k].roles.includes(role)) || 'dashboard';
    mod = MODULES[name];
    location.hash = '#' + name;
    return;
  }

  currentRoute = name;
  $('#pageTitle').textContent = mod.title;
  $('#pageSub').textContent = mod.sub;

  /* active states */
  $all('#sideNav a').forEach(a => a.classList.toggle('active', a.dataset.route === name));
  const activeSub = $('#sideNav a[data-route="' + name + '"]');
  if (activeSub) {
    const groupBody = activeSub.closest('.subnav');
    if (groupBody) {
      groupBody.classList.add('open');
      const toggle = $('.group-toggle[data-group="' + groupBody.dataset.groupBody + '"]');
      if (toggle) toggle.classList.add('active');
    }
  }

  const view = $('#view');
  view.innerHTML = '<div class="state"><div class="spin"></div>Loading module…</div>';
  $('#sidebar').classList.remove('open'); // close mobile drawer after navigation

  try {
    const src = 'js/views/' + (mod.view.split('/').pop());
    await loadScript(src);
    await window['view_' + name.replace(/-([a-z])/g, (m, c) => c.toUpperCase())](view);
  } catch (err) {
    view.innerHTML = '<div class="card"><div class="card-body"><div class="state error">' +
      '<div class="big">⚠️</div><div class="msg">Failed to load module</div>' +
      '<div class="sub">' + esc(err.message) + '</div>' +
      '<button class="btn ghost sm" onclick="location.reload()">Reload</button></div></div></div>';
  }
}

function loadScript(src) {
  return new Promise((resolve, reject) => {
    if (document.querySelector('script[data-view="' + src + '"]')) { resolve(); return; }
    const s = document.createElement('script');
    s.src = src + '?v=' + Date.now();
    s.dataset.view = src;
    s.onload = resolve;
    s.onerror = () => reject(new Error('Could not load ' + src));
    document.body.appendChild(s);
  });
}

/* ---------- user chip ---------- */
async function loadUserChip() {
  $('#userName').textContent = getUsername() || 'User';
  $('#userRole').textContent = getRole() || '';
  $('#userAvatar').textContent = initials(getUsername());
  try {
    const me = await api('/api/auth/me');
    if (me.studentId) sessionStorage.setItem('spis_studentId', me.studentId);
  } catch (e) { /* chip stays as-is */ }
}

/* ---------- notification panel (live data, no backend sending service) ---------- */
async function renderNotifPanel() {
  const body = $('#notifBody');
  const role = getRole();
  body.innerHTML = '<div class="state"><div class="spin"></div>Checking live academic data…</div>';
  const items = await computeAlerts(role);
  $('#notifDot').style.display = 'none';
  if (!items.length) {
    body.innerHTML = '<div class="state"><div class="big">✅</div><div class="msg">Nothing needs attention</div>' +
      '<div class="sub">No low-attendance or low-marks alerts from live data.</div></div>';
    return;
  }
  body.innerHTML = items.map(n =>
    '<div class="list-tile">' +
    '<div class="ico" style="background:' + (n.cls === 'red' ? '#fef2f2' : '#fffbeb') + '">' + n.icon + '</div>' +
    '<div class="grow"><b>' + esc(n.title) + '</b><span>' + esc(n.detail) + '</span></div>' +
    '<span class="badge ' + n.cls + '">' + n.tag + '</span></div>'
  ).join('');
}

/* Derive alerts from real backend data (attendance <75%, marks <40%).
   These are in-app notifications computed on demand — no email/SMS/push is sent. */
async function computeAlerts(role) {
  const items = [];
  try {
    if (role === 'STUDENT') {
      const att = await api('/api/students/me/attendance-percentage').catch(() => null);
      const avg = await api('/api/students/me/average-marks').catch(() => null);
      if (att != null && att < 75) items.push({ icon: '🚨', cls: 'red', tag: 'Attendance', title: 'Low attendance', detail: 'Your attendance is ' + pct(att) + ' (below the 75% requirement).' });
      if (avg != null && avg < 40) items.push({ icon: '📉', cls: 'amber', tag: 'Marks', title: 'Low average marks', detail: 'Your current average is ' + num(avg, 1) + '.' });
      if (!att && !avg) items.push({ icon: '📭', cls: 'amber', tag: 'No data', title: 'No academic records yet', detail: 'Attendance and marks appear once your faculty records them.' });
      return items;
    }
    const summary = await api('/api/dashboard/summary');
    if (summary.studentsBelowAttendanceThreshold > 0) {
      items.push({ icon: '🚨', cls: 'red', tag: 'Attendance', title: summary.studentsBelowAttendanceThreshold + ' student(s) below 75% attendance', detail: 'Open Attendance or Analytics to review them.' });
    }
    if (summary.studentsBelowMarksThreshold > 0) {
      items.push({ icon: '📉', cls: 'amber', tag: 'Marks', title: summary.studentsBelowMarksThreshold + ' student(s) below marks threshold', detail: 'Open Analytics → At-risk students to review them.' });
    }
  } catch (e) { /* bell is best-effort */ }
  return items;
}

function prefetchNotifCount() {
  computeAlerts(getRole()).then(items => {
    if (items.length) {
      $('#notifDot').style.display = 'flex';
      $('#notifDot').textContent = items.length;
    }
  });
}

/* ---------- boot ---------- */
window.addEventListener('hashchange', route);
initModals(); // wire all static modals (incl. #notifPanel) once at boot — idempotent
buildNav();
loadUserChip().then(route);
