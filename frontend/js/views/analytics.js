/* Analytics module — performance, attendance and student analytics from real APIs */
window.view_analytics = async function (view) {
  view.innerHTML =
    '<div class="page-head"><div><h2>Analytics</h2><div class="desc">Charts and threshold analysis computed by the backend from live academic data.</div></div>' +
    '<div class="page-actions"><button class="btn ghost" id="exportBtn">⬇ Export current view</button></div></div>' +

    '<div class="tabs">' +
    '  <button data-tab="perf" class="active">Performance</button>' +
    '  <button data-tab="att">Attendance</button>' +
    '  <button data-tab="stud">Students</button>' +
    '</div>' +

    '<div id="tab-perf">' +
    '  <div class="kpis" id="perfKpi"></div>' +
    '  <div class="grid-2-1">' +
    '    <div class="card"><div class="card-body"><h3 class="card-title">Average marks by department</h3><div id="deptChart"></div></div></div>' +
    '    <div class="card"><div class="card-body"><h3 class="card-title">Average marks by subject</h3><div id="subjChart"></div></div></div>' +
    '  </div>' +
    '</div>' +

    '<div id="tab-att" style="display:none">' +
    '  <div class="kpis" id="attKpi"></div>' +
    '  <div class="grid-2-1">' +
    '    <div class="card"><div class="card-body"><h3 class="card-title">Attendance % by subject</h3><div id="attSubjChart"></div></div></div>' +
    '    <div class="card"><div class="card-body"><h3 class="card-title">Attendance % by department</h3><div id="attDeptChart"></div></div></div>' +
    '  </div>' +
    '</div>' +

    '<div id="tab-stud" style="display:none">' +
    '  <div class="grid-2 mb">' +
    '    <div class="card"><div class="card-body">' +
    '      <h3 class="card-title">Top performers (ranking API)</h3><div id="topList" class="muted">Loading…</div></div></div>' +
    '    <div class="card"><div class="card-body">' +
    '      <h3 class="card-title">Attendance threshold analysis</h3>' +
    '      <div class="toolbar"><label class="muted">Threshold %</label>' +
    '      <select id="attThreshold">' + [90, 85, 80, 75, 70, 60, 50].map(t => '<option ' + (t === 75 ? 'selected' : '') + '>' + t + '</option>').join('') + '</select></div>' +
    '      <div id="belowAtt" class="muted">Loading…</div></div></div>' +
    '  </div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Declining / at-risk students <span class="muted" style="font-weight:400">(marks threshold)</span></h3>' +
    '    <div class="toolbar"><label class="muted">Marks threshold</label>' +
    '    <select id="marksThreshold"><option>35</option><option selected>40</option><option>45</option><option>50</option></select></div>' +
    '    <div id="belowMarks" class="muted">Loading…</div>' +
    '  </div></div>' +
    '</div>';

  let currentExport = null;

  $all('.tabs button').forEach(b => b.addEventListener('click', () => {
    $all('.tabs button').forEach(x => x.classList.remove('active'));
    b.classList.add('active');
    ['perf', 'att', 'stud'].forEach(t => $('#tab-' + t).style.display = t === b.dataset.tab ? '' : 'none');
  }));

  /* ---------- performance tab ---------- */
  async function loadPerf() {
    const summary = await api('/api/dashboard/summary');
    const deptPerf = await api('/api/reports/departments');
    const subjPerf = await api('/api/reports/subjects');

    $('#perfKpi').innerHTML = [
      ['Overall Avg Marks', num(summary.overAllAverageMarks), 'blue'],
      ['Overall Attendance', pct(summary.overAllAttendancePercentage), 'green'],
      ['Students', summary.totalStudents, 'violet'],
      ['Subjects', summary.totalSubjects, 'amber']
    ].map(k => '<div class="kpi ' + k[2] + '"><div class="label">' + k[0] + '</div><div class="value">' + k[1] + '</div></div>').join('');

    renderBarChart($('#deptChart'), deptPerf.filter(d => d.averageMarks != null)
      .map(d => ({ label: d.departmentName, value: d.averageMarks }))
      .sort((a, b) => b.value - a.value), { max: 100 });
    renderBarChart($('#subjChart'), subjPerf.filter(s => s.averageMarks != null)
      .map(s => ({ label: s.subjectCode, value: s.averageMarks }))
      .sort((a, b) => b.value - a.value).slice(0, 10), { max: 100 });

    currentExport = {
      name: 'analytics-performance',
      columns: [
        { label: 'Department', value: 'dept' }, { label: 'Avg Marks', value: 'marks' }, { label: 'Avg Attendance %', value: 'att' }
      ],
      rows: deptPerf.map(d => ({ dept: d.departmentName, marks: d.averageMarks != null ? Number(d.averageMarks.toFixed(1)) : '', att: d.averageAttendance != null ? Number(d.averageAttendance.toFixed(1)) : '' }))
    };
  }

  /* ---------- attendance tab ---------- */
  async function loadAtt() {
    const summary = await api('/api/dashboard/summary');
    const [depts, subjects] = await Promise.all([api('/api/departments'), api('/api/subjects')]);

    $('#attKpi').innerHTML = [
      ['Overall Attendance', pct(summary.overAllAttendancePercentage), 'green'],
      ['Below 75% (students)', summary.studentsBelowAttendanceThreshold, 'red'],
      ['Departments', depts.length, 'blue'],
      ['Subjects', subjects.length, 'violet']
    ].map(k => '<div class="kpi ' + k[2] + '"><div class="label">' + k[0] + '</div><div class="value">' + k[1] + '</div></div>').join('');

    const subjPerc = await Promise.all(subjects.map(s =>
      api('/api/analytics/subject/' + s.subjectId + '/subject-attendance-percentage').catch(() => null)));
    renderBarChart($('#attSubjChart'), subjects.map((s, i) => ({ label: s.subjectCode, value: subjPerc[i] || 0 }))
      .sort((a, b) => b.value - a.value), { suffix: '%', max: 100, decimals: 0 });

    const deptPerc = await Promise.all(depts.map(d =>
      api('/api/analytics/department/' + d.departmentId + '/department-attendance-percentage').catch(() => null)));
    renderBarChart($('#attDeptChart'), depts.map((d, i) => ({ label: d.departmentCode, value: deptPerc[i] || 0 })),
      { suffix: '%', max: 100, decimals: 0 });

    currentExport = {
      name: 'analytics-attendance',
      columns: [
        { label: 'Subject', value: 'subject' }, { label: 'Attendance %', value: 'att' }
      ],
      rows: subjects.map((s, i) => ({ subject: s.subjectName, att: subjPerc[i] != null ? Number(subjPerc[i].toFixed(1)) : '' }))
    };
  }

  /* ---------- students tab ---------- */
  async function loadStudentsTab() {
    const ranking = await api('/api/reports/student-ranking');
    const top = ranking.slice(0, 10);
    $('#topList').innerHTML = top.length ? top.map(r =>
      '<div class="list-tile"><div class="ico">' + (r.rank <= 3 ? '🥇' : '🎓') + '</div>' +
      '<div class="grow"><b>#' + r.rank + ' ' + esc(r.studentName) + '</b><span>' + esc(r.rollNumber) + ' · ' + esc(r.department) + '</span></div>' +
      marksBadge(r.averageMarks) + '</div>').join('')
      : '<div class="state"><div class="big">📭</div><div class="msg">No ranking data</div></div>';

    async function belowAtt() {
      const t = $('#attThreshold').value;
      const list = await api('/api/analytics/student/below-attendance/' + t);
      $('#belowAtt').innerHTML = list.length
        ? '<div class="table-wrap"><table class="data"><thead><tr><th>Roll No</th><th>Name</th><th>Email</th></tr></thead><tbody>' +
          list.map(st => '<tr><td>' + esc(st.rollNumber) + '</td><td>' + esc(fullName(st)) + '</td><td class="muted">' + esc(st.email) + '</td></tr>').join('') +
          '</tbody></table></div>'
        : '<div class="state"><div class="big">🎉</div><div class="msg">No students below ' + t + '%</div></div>';
    }
    async function belowMarks() {
      const t = $('#marksThreshold').value;
      const list = await api('/api/analytics/student/below-marks/' + t);
      $('#belowMarks').innerHTML = list.length
        ? '<div class="table-wrap"><table class="data"><thead><tr><th>Roll No</th><th>Name</th><th>Email</th></tr></thead><tbody>' +
          list.map(st => '<tr><td>' + esc(st.rollNumber) + '</td><td>' + esc(fullName(st)) + '</td><td class="muted">' + esc(st.email) + '</td></tr>').join('') +
          '</tbody></table></div>'
        : '<div class="state"><div class="big">🎉</div><div class="msg">No students below ' + t + ' marks</div></div>';
    }
    $('#attThreshold').addEventListener('change', belowAtt);
    $('#marksThreshold').addEventListener('change', belowMarks);
    await Promise.all([belowAtt(), belowMarks()]);

    currentExport = {
      name: 'analytics-students',
      columns: [
        { label: 'Rank', value: 'rank' }, { label: 'Name', value: 'name' }, { label: 'Roll No', value: 'roll' },
        { label: 'Department', value: 'dept' }, { label: 'Average Marks', value: 'marks' }
      ],
      rows: ranking.map(r => ({ rank: r.rank, name: r.studentName, roll: r.rollNumber, dept: r.department, marks: r.averageMarks }))
    };
  }

  $('#exportBtn').onclick = async () => {
    if (!currentExport) { toast('Load a tab first.', 'warn'); return; }
    await exportWithPicker(currentExport.name, currentExport.columns, currentExport.rows);
  };

  try {
    await loadPerf();
  } catch (e) {
    $('#perfKpi').innerHTML = '<div class="kpi red"><div class="label">Error</div><div class="value">!</div></div>';
    toast(e.message, 'error', 'Analytics failed');
  }
  /* preload other tabs lazily on first switch */
  let attLoaded = false, studLoaded = false;
  $all('.tabs button').forEach(b => b.addEventListener('click', async () => {
    try {
      if (b.dataset.tab === 'att' && !attLoaded) { attLoaded = true; await loadAtt(); }
      if (b.dataset.tab === 'stud' && !studLoaded) { studLoaded = true; await loadStudentsTab(); }
    } catch (e) { toast(e.message, 'error'); }
  }));
};
