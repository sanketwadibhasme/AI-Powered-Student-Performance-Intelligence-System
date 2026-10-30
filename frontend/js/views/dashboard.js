/* Role-aware dashboard — one module serving ADMIN / FACULTY / STUDENT */
window.view_dashboard = async function (view) {
  const role = getRole();

  if (role === 'STUDENT') return studentDashboard(view);
  if (role === 'FACULTY') return facultyWorkspace(view);
  return staffDashboard(view, role);
};

/* ================= FACULTY — daily academic workspace =================
   Answers, in order: What do I teach? Who needs attention today?
   Who is declining? Where do I work next?                              */
async function facultyWorkspace(view) {
  view.innerHTML = '<div class="state"><div class="spin"></div>Loading your workspace…</div>';

  /* subjects taught by this faculty (subject-first workflow) */
  let mySubjects = [];
  let facultyLinkError = '';
  try {
    mySubjects = await api('/api/faculties/me/subjects');
  } catch (e) {
    facultyLinkError = e.message;
  }

  const [summary, belowAtt, belowMarks, ranking, alerts] = await Promise.all([
    api('/api/dashboard/summary'),
    api('/api/analytics/student/below-attendance/75').catch(() => []),
    api('/api/analytics/student/below-marks/40').catch(() => []),
    api('/api/reports/student-ranking').catch(() => []),
    computeAlerts('FACULTY')
  ]);

  const flaggedIds = new Set([...belowAtt.map(s => s.studentId), ...belowMarks.map(s => s.studentId)]);

  view.innerHTML =
    '<div class="page-head"><div><h2>Faculty Workspace</h2>' +
    '<div class="desc">Your subjects for today\u2019s academic work — attendance, marks, and students needing attention.</div></div>' +
    '<div class="page-actions">' +
    '<a class="btn ghost" href="#attendance">Take Attendance</a>' +
    '<a class="btn primary" href="#marks">Enter Marks</a>' +
    '</div></div>' +

    (facultyLinkError
      ? '<div class="card mb"><div class="card-body"><div class="state error"><div class="big">🔗</div>' +
        '<div class="msg">Faculty record not linked to your account</div><div class="sub">' + esc(facultyLinkError) +
        ' Your account email must match a faculty record\u2019s email. Subject shortcuts are unavailable; everything else works.</div></div></div></div>'
      : '') +

    '<div class="kpis">' +
    '  <div class="kpi blue"><div class="label">📚 My Subjects</div><div class="value">' + mySubjects.length + '</div></div>' +
    '  <div class="kpi violet"><div class="label">🎓 Total Students</div><div class="value">' + (summary.totalStudents ?? '—') + '</div></div>' +
    '  <div class="kpi red"><div class="label">🚨 Low Attendance</div><div class="value">' + (summary.studentsBelowAttendanceThreshold ?? '—') + '</div></div>' +
    '  <div class="kpi amber"><div class="label">📉 Low Marks</div><div class="value">' + (summary.studentsBelowMarksThreshold ?? '—') + '</div></div>' +
    '</div>' +

    '<div class="card mb"><div class="card-body">' +
    '  <h3 class="card-title">My subjects <span class="muted" style="font-weight:400">— pick one to work with its class</span></h3>' +
    (mySubjects.length
      ? '<div class="grid-2">' + mySubjects.map(s =>
          '<div class="card"><div class="card-body" style="padding:14px 16px">' +
          '<div class="flex"><div><b>' + esc(s.subjectName) + '</b>' +
          '<div class="help">' + esc(s.subjectCode) + ' · ' + esc(s.department ? s.department.departmentName : '') + ' · ' + s.credits + ' credits</div></div></div>' +
          '<div class="flex mt" style="gap:8px">' +
          '<a class="btn primary sm" href="#attendance" onclick="sessionStorage.setItem(\'spis_subject\',\'' + s.subjectId + '\')">✋ Attendance</a>' +
          '<a class="btn primary sm" href="#marks" onclick="sessionStorage.setItem(\'spis_subject\',\'' + s.subjectId + '\')">✏️ Marks</a>' +
          '<a class="btn ghost sm" href="#enrollments" onclick="sessionStorage.setItem(\'spis_subject\',\'' + s.subjectId + '\')">👥 Class</a>' +
          '</div></div></div>').join('') + '</div>'
      : '<div class="state"><div class="big">📚</div><div class="msg">No subjects assigned to you yet</div>' +
        '<div class="sub">Ask the administrator to assign you as the faculty of a subject (Subjects → edit → faculty).</div></div>') +
    '</div></div>' +

    '<div class="grid-2 mb">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Students needing attention <a class="right" href="#ai">Full AI analysis →</a></h3>' +
    (flaggedIds.size
      ? '<div class="table-wrap"><table class="data"><thead><tr><th>Roll No</th><th>Student</th><th>Reason</th><th>Action</th></tr></thead><tbody>' +
        studentsNeedingAttention(belowAtt, belowMarks).map(r =>
          '<tr><td>' + esc(r.rollNumber) + '</td><td>' + esc(r.name) + '</td>' +
          '<td>' + r.reasons.map(x => '<span class="badge ' + x.cls + '">' + esc(x.label) + '</span>').join(' ') + '</td>' +
          '<td class="actions"><button class="btn ghost sm" data-view-student="' + r.id + '">Open</button>' +
          '<button class="btn violet sm" data-ai-student="' + r.id + '">🤖 Analyze</button></td></tr>').join('') +
        '</tbody></table></div>'
      : '<div class="state"><div class="big">✅</div><div class="msg">No one is below thresholds</div><div class="sub">Attendance ≥ 75% and marks ≥ 40 across the system.</div></div>') +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Important alerts</h3>' +
    (alerts.length
      ? alerts.map(n => '<div class="list-tile"><div class="ico" style="background:' + (n.cls === 'red' ? '#fef2f2' : '#fffbeb') + '">' + n.icon + '</div>' +
          '<div class="grow"><b>' + esc(n.title) + '</b><span>' + esc(n.detail) + '</span></div></div>').join('')
      : '<div class="state"><div class="big">✅</div><div class="msg">Nothing needs attention</div></div>') +
    '  </div></div>' +
    '</div>' +

    '<div class="grid-2">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Top students <a class="right" href="#rankings">View all →</a></h3>' +
    '    <div class="table-wrap"><table class="data"><thead><tr><th>#</th><th>Student</th><th>Dept</th><th>Avg</th></tr></thead><tbody>' +
    (ranking.slice(0, 5).map(r => '<tr><td><b>' + r.rank + '</b></td><td>' + esc(r.studentName) + '</td><td class="muted">' + esc(r.department) + '</td><td>' + marksBadge(r.averageMarks) + '</td></tr>').join('') ||
      '<tr><td colspan="4" class="muted">No marks recorded yet.</td></tr>') +
    '    </tbody></table></div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Quick reference</h3>' +
    '    <div class="list-tile"><div class="ico">🗓️</div><div class="grow"><b>Institution average attendance</b><span>' + pct(summary.overAllAttendancePercentage) + '</span></div></div>' +
    '    <div class="list-tile"><div class="ico">📈</div><div class="grow"><b>Institution average marks</b><span>' + num(summary.overAllAverageMarks) + '</span></div></div>' +
    '    <div class="list-tile"><div class="ico">🤖</div><div class="grow"><b>AI Intelligence</b><span>Run per-student predictions and what-if simulations.</span></div>' +
    '      <span id="aiHealthPill" class="badge gray" style="cursor:default">AI …</span></div>' +
    '  </div></div>' +
    '</div>';

  /* action buttons */
  view.addEventListener('click', e => {
    const viewBtn = e.target.closest('button[data-view-student]');
    const aiBtn = e.target.closest('button[data-ai-student]');
    if (viewBtn) {
      sessionStorage.setItem('spis_selectedStudent', viewBtn.dataset.viewStudent);
      location.hash = '#student';
    } else if (aiBtn) {
      sessionStorage.setItem('spis_selectedStudent', aiBtn.dataset.aiStudent);
      location.hash = '#student';
      toast('Opening the student profile — press "Run AI Analysis" there.', 'info', 'AI analysis');
    }
  });

  refreshAIHealth();
}

function studentsNeedingAttention(belowAtt, belowMarks) {
  const map = {};
  const add = (s, label, cls) => {
    if (!map[s.studentId]) map[s.studentId] = {
      id: s.studentId, rollNumber: s.rollNumber, name: fullName(s), reasons: []
    };
    map[s.studentId].reasons.push({ label, cls });
  };
  belowAtt.forEach(s => add(s, 'Attendance < 75%', 'red'));
  belowMarks.forEach(s => add(s, 'Marks < 40', 'amber'));
  return Object.values(map).slice(0, 8);
}

/* ================= ADMIN ================= */
async function staffDashboard(view, role) {
  view.innerHTML = '<div class="state"><div class="spin"></div>Loading dashboard…</div>';

  const summary = await api('/api/dashboard/summary');
  const [deptPerf, ranking, belowAtt, alerts] = await Promise.all([
    api('/api/dashboard/departments-summary'),
    api('/api/reports/student-ranking').catch(() => []),
    api('/api/analytics/student/below-attendance/75').catch(() => []),
    computeAlerts(role)
  ]);

  view.innerHTML =
    '<div class="page-head"><div><h2>Admin Workspace</h2>' +
    '<div class="desc">System-wide management: students, faculty, departments, users and intelligence.</div></div>' +
    '<div class="page-actions">' +
    '<a class="btn ghost" href="#users">Manage Users</a><a class="btn primary" href="#students">Manage Students</a>' +
    '</div></div>' +

    '<div class="kpis">' +
    [['Total Students', summary.totalStudents, 'blue', '🎓'],
     ['Total Faculty', summary.totalFaculty, 'violet', '👨‍🏫'],
     ['Departments', summary.totalDepartments, 'amber', '🏛️'],
     ['Subjects', summary.totalSubjects, 'green', '📚'],
     ['Avg Marks', num(summary.overAllAverageMarks), 'blue', '📈'],
     ['Avg Attendance', pct(summary.overAllAttendancePercentage), 'green', '🗓️'],
     ['Below 75% Attendance', summary.studentsBelowAttendanceThreshold, 'red', '🚨'],
     ['Below Marks Threshold', summary.studentsBelowMarksThreshold, 'amber', '📉']
    ].map(k => '<div class="kpi ' + k[2] + '"><div class="label">' + k[3] + ' ' + k[0] + '</div><div class="value">' + (k[1] ?? '—') + '</div></div>').join('') +
    '</div>' +

    '<div class="grid-2-1 mb">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Department performance</h3><div id="deptChart"></div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Important alerts</h3>' +
    (alerts.length
      ? alerts.map(n => '<div class="list-tile"><div class="ico" style="background:' + (n.cls === 'red' ? '#fef2f2' : '#fffbeb') + '">' + n.icon + '</div>' +
          '<div class="grow"><b>' + esc(n.title) + '</b><span>' + esc(n.detail) + '</span></div></div>').join('') +
        '<a class="btn ghost sm mt" href="#ai">Open AI Intelligence →</a>'
      : '<div class="state"><div class="big">✅</div><div class="msg">Nothing needs attention</div></div>') +
    '  </div></div>' +
    '</div>' +

    '<div class="grid-2">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Top students <a class="right" href="#rankings">View all →</a></h3>' +
    '    <div class="table-wrap"><table class="data"><thead><tr><th>#</th><th>Student</th><th>Dept</th><th>Avg</th></tr></thead><tbody>' +
    (ranking.slice(0, 5).map(r => '<tr><td><b>' + r.rank + '</b></td><td>' + esc(r.studentName) + '</td><td class="muted">' + esc(r.department) + '</td><td>' + marksBadge(r.averageMarks) + '</td></tr>').join('') ||
      '<tr><td colspan="4" class="muted">No marks recorded yet.</td></tr>') +
    '    </tbody></table></div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Low attendance watchlist <a class="right" href="#attendance">Open module →</a></h3>' +
    (belowAtt.length
      ? '<div class="table-wrap"><table class="data"><thead><tr><th>Roll No</th><th>Student</th></tr></thead><tbody>' +
        belowAtt.slice(0, 5).map(st => '<tr><td>' + esc(st.rollNumber) + '</td><td>' + esc(fullName(st)) + '</td></tr>').join('') +
        '</tbody></table></div>'
      : '<div class="state"><div class="big">🎉</div><div class="msg">All students above 75%</div></div>') +
    '  </div></div>' +
    '</div>';

  renderBarChart($('#deptChart'),
    deptPerf.filter(d => d.averageMarks != null).map(d => ({ label: d.departmentName, value: d.averageMarks })),
    { max: 100 });
}

/* ================= STUDENT ================= */
async function studentDashboard(view) {
  view.innerHTML = '<div class="state"><div class="spin"></div>Loading your dashboard…</div>';

  const [me, avg, att] = await Promise.all([
    api('/api/students/me/profile'),
    api('/api/students/me/average-marks').catch(() => null),
    api('/api/students/me/attendance-percentage').catch(() => null)
  ]);
  const [marks, attendanceList, enrollments, myAlerts] = await Promise.all([
    api('/api/students/me/marks').catch(() => []),
    api('/api/students/me/attendance').catch(() => []),
    api('/api/students/me/enrollments').catch(() => []),
    computeAlerts('STUDENT')
  ]);

  const hasMarks = marks.length > 0;

  /* upcoming exams from the shared exam list */
  const today = new Date().toISOString().slice(0, 10);
  let exams = [];
  try {
    exams = (await api('/api/exams')).filter(x => x.examDate >= today).sort((a, b) => a.examDate.localeCompare(b.examDate)).slice(0, 5);
  } catch (e) { /* student may lack access; ignore */ }

  /* my rank */
  let rank = null;
  try {
    const ranking = await api('/api/reports/student-ranking');
    const row = ranking.find(r => r.studentId === Number(sessionStorage.getItem('spis_studentId')));
    if (row) rank = row.rank;
  } catch (e) { /* ignore */ }

  view.innerHTML =
    '<div class="page-head"><div><h2>Hi, ' + esc(me.firstName) + ' 👋</h2>' +
    '<div class="desc">' + esc(me.rollNumber) + ' · ' + esc(me.department ? me.department.departmentName : '') + ' · Semester ' + me.currentSemester + '</div></div>' +
    '<div class="page-actions"><a class="btn ghost" href="#myacademics">My Academics</a>' +
    '<a class="btn violet" href="#myai">🤖 My AI Insights</a></div></div>' +

    '<div class="kpis">' +
    '  <div class="kpi blue"><div class="label">📈 Average Marks</div><div class="value">' + (avg != null ? num(avg) : '—') + '</div></div>' +
    '  <div class="kpi ' + (att != null && att < 75 ? 'red' : 'green') + '"><div class="label">🗓️ Attendance</div><div class="value">' + (att != null ? pct(att) : '—') + '</div></div>' +
    '  <div class="kpi violet"><div class="label">📚 Subjects</div><div class="value">' + enrollments.length + '</div></div>' +
    '  <div class="kpi amber"><div class="label">🏆 Class Rank</div><div class="value">' + (rank ? '#' + rank : '—') + '</div></div>' +
    '</div>' +

    (!hasMarks
      ? '<div class="card mb"><div class="card-body"><div class="state"><div class="big">📭</div>' +
        '<div class="msg">No marks recorded yet</div><div class="sub">Once your faculty records assessment marks, your averages, trends and AI insights will appear here.</div></div></div></div>'
      : '') +
    (!attendanceList.length
      ? '<div class="card mb"><div class="card-body"><div class="state"><div class="big">🗓️</div>' +
        '<div class="msg">No attendance records yet</div><div class="sub">Your attendance percentage will appear once sessions are recorded.</div></div></div></div>'
      : '') +

    (myAlerts.length
      ? '<div class="card mb"><div class="card-body">' + myAlerts.map(n =>
          '<div class="list-tile"><div class="ico" style="background:#fef2f2">' + n.icon + '</div>' +
          '<div class="grow"><b>' + esc(n.title) + '</b><span>' + esc(n.detail) + '</span></div>' +
          '<span class="badge ' + n.cls + '">' + n.tag + '</span></div>').join('') + '</div></div>'
      : '') +

    '<div class="grid-2-1 mb">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">My performance trend (by exam)</h3>' +
    (hasMarks ? '<div id="trend"></div>' : '<div class="state"><div class="big">📊</div><div class="msg">No data to chart</div></div>') +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Upcoming exams</h3>' +
    (exams.length
      ? exams.map(x => '<div class="list-tile"><div class="ico">📝</div><div class="grow"><b>' + esc(x.examName) + '</b><span>' + fmtDate(x.examDate) + ' · total ' + x.totalMarks + '</span></div></div>').join('')
      : '<div class="state"><div class="big">📅</div><div class="msg">No upcoming exams</div></div>') +
    '  </div></div>' +
    '</div>' +

    '<div class="grid-2">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Recent marks <a class="right" href="#myacademics">All →</a></h3>' +
    '    <div class="table-wrap"><table class="data"><thead><tr><th>Subject</th><th>Exam</th><th>Score</th></tr></thead><tbody>' +
    (marks.slice(-6).reverse().map(m => '<tr><td>' + esc(m.subject ? m.subject.subjectName : '—') + '</td><td>' + esc(m.exam ? m.exam.examName : '—') + '</td><td>' + marksBadge(m.marksObtained) + '</td></tr>').join('') ||
      '<tr><td colspan="3" class="muted">No marks yet.</td></tr>') +
    '    </tbody></table></div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">AI insight <span id="aiHealthPill" class="badge gray" style="margin-left:6px;cursor:default">AI …</span></h3>' +
    '    <div id="aiMini" class="muted">Runs on your real marks &amp; attendance when you click below.</div>' +
    '    <button class="btn violet mt" id="aiBtn">🤖 Predict my performance</button>' +
    '  </div></div>' +
    '</div>';

  /* trend chart */
  if (hasMarks) {
    const byExam = {};
    marks.forEach(m => {
      if (!m.exam) return;
      if (!byExam[m.exam.examId]) byExam[m.exam.examId] = { name: m.exam.examName, date: m.exam.examDate, sum: 0, n: 0 };
      byExam[m.exam.examId].sum += m.marksObtained; byExam[m.exam.examId].n++;
    });
    renderBarChart($('#trend'), Object.values(byExam)
      .sort((a, b) => String(a.date).localeCompare(String(b.date)))
      .map(e => ({ label: e.name, value: e.n ? e.sum / e.n : 0, cls: 'green' })), { max: 100 });
  }

  /* mini AI */
  $('#aiBtn').onclick = async () => {
    const out = $('#aiMini');
    out.innerHTML = '<div class="flex"><div class="spin" style="margin:0"></div> Running model…</div>';
    const result = await runAI($('#aiBtn'), '/api/ai/me/insights', {}, { target: null, onError: 'toast' });
    out.innerHTML = result
      ? '<div class="flex mt" style="gap:18px">' +
        '<div><div class="muted">Predicted score</div><b style="font-size:22px">' + num(result.predictedScore) + '<span class="muted" style="font-size:12px">/100</span></b> ' +
        '<span class="badge ' + (result.performanceCategory === 'POOR' ? 'red' : result.performanceCategory === 'AVERAGE' ? 'amber' : 'green') + '">' + esc(result.performanceCategory || '') + '</span></div>' +
        '<div><div class="muted">Academic risk</div><b style="font-size:22px">' + (result.riskProbability != null ? (result.riskProbability * 100).toFixed(0) + '%' : '—') + '</b> ' +
        '<span class="badge ' + (result.riskLevel === 'HIGH' ? 'red' : result.riskLevel === 'MEDIUM' ? 'amber' : 'green') + '">' + esc(result.riskLevel || '') + '</span></div></div>' +
        '<div class="help mt">Data quality: ' + esc(result.dataQuality || '—') + ' · ' + esc((result.dataBasis || []).join('; ') || '') + '</div>' +
        '<a class="btn ghost sm mt" href="#myai">Full analysis →</a>'
      : '<div class="muted">Prediction unavailable.</div>';
  };
}
