/* AI Intelligence — Admin/Faculty: at-risk overview + per-student prediction with what-if */
window.view_ai = async function (view) {
  let students = [];

  view.innerHTML =
    '<div class="page-head"><div><h2>AI Intelligence</h2>' +
    '<div class="desc">Predictions use trained Random Forest models (UCI Student Performance dataset) applied to each student\u2019s real marks &amp; attendance. <span id="aiHealthPill" class="badge gray" style="margin-left:6px;cursor:default">AI …</span></div></div></div>' +

    '<div class="kpis" id="kpi"></div>' +

    '<div class="card mb"><div class="card-body">' +
    '<h3 class="card-title">Students requiring attention <span class="muted" style="font-weight:400">(thresholds from live academic data)</span></h3>' +
    '<div class="table-wrap"><table class="data"><thead><tr>' +
    '<th>Student</th><th>Roll No</th><th>Department</th><th>Attendance</th><th>Avg Marks</th><th>Signals</th><th>Action</th>' +
    '</tr></thead><tbody id="riskRows"></tbody></table></div>' +
    '</div></div>' +

    '<div class="card"><div class="card-body">' +
    '<h3 class="card-title">Run a prediction</h3>' +
    '<div class="help mb">Select a student — the system loads their real academic data automatically. What-if simulation is separate below.</div>' +
    '<div class="form-grid">' +
    '  <div class="field"><label>Student</label><select id="aiStudent"><option value="">Loading…</option></select></div>' +
    '  <div class="field"><label>&nbsp;</label><button class="btn violet" id="runBtn">🤖 Run Prediction on Real Data</button></div>' +
    '</div>' +
    '<div id="aiOut" class="mt"></div>' +
    '</div></div>' +

    '<div class="card mt"><div class="card-body">' +
    '<h3 class="card-title">What-if simulation <span class="badge gray" style="margin-left:6px">Simulation only</span></h3>' +
    '<div class="help mb">Change values to explore a hypothetical scenario. Nothing is saved to the student\u2019s records, and the change shown is a model prediction difference — not a causal effect.</div>' +
    '<div class="form-grid">' +
    '  <div class="field"><label>First assessment score (0-100)</label><input id="wG1" type="number" min="0" max="100"></div>' +
    '  <div class="field"><label>Recent assessment score (0-100)</label><input id="wG2" type="number" min="0" max="100"></div>' +
    '  <div class="field"><label>Study time (1 &lt;2h/wk · 2: 2-5h · 3: 5-10h · 4: &gt;10h)</label>' +
    '    <select id="wStudy"><option value="">leave as is</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select></div>' +
    '  <div class="field"><label>Absences</label><input id="wAbs" type="number" min="0"></div>' +
    '</div>' +
    '<button class="btn ghost mt" id="simBtn">▶ Run Simulation</button>' +
    '<div id="simOut" class="mt"></div>' +
    '</div></div>';

  async function loadStudents() {
    students = await api('/api/students');
    fillSelect($('#aiStudent'), students, 'studentId',
      s => s.rollNumber + ' — ' + fullName(s) + (s.department ? ' (' + s.department.departmentName + ')' : ''), 'Select a student…');
  }

  function signalBadges(st) {
    const b = [];
    if (st.attendance != null && st.attendance < 75) b.push('<span class="badge red">Attendance ' + st.attendance.toFixed(0) + '%</span>');
    if (st.avgMarks != null && st.avgMarks < 40) b.push('<span class="badge amber">Marks ' + st.avgMarks.toFixed(0) + '</span>');
    if (!b.length) b.push('<span class="badge gray">None</span>');
    return b.join(' ');
  }

  async function loadRiskList() {
    const tbody = $('#riskRows');
    renderLoading(tbody, 7);
    try {
      const [perf, belowAtt, belowMarks] = await Promise.all([
        api('/api/reports/students'),
        api('/api/analytics/student/below-attendance/75').catch(() => []),
        api('/api/analytics/student/below-marks/40').catch(() => [])
      ]);
      const perfById = {}; perf.forEach(p => { perfById[p.studentId] = p; });

      const riskIds = new Set([...belowAtt.map(s => s.studentId), ...belowMarks.map(s => s.studentId)]);
      const risky = students.filter(s => riskIds.has(s.studentId));

      // KPI counts: students analyzed = all, risk buckets from the at-risk set are
      // labelled clearly as threshold-based (ML risk levels are per-student runs)
      const summary = await api('/api/dashboard/summary');
      $('#kpi').innerHTML = [
        ['Students Analyzed', students.length, 'blue'],
        ['Below 75% Attendance', summary.studentsBelowAttendanceThreshold ?? '—', 'red'],
        ['Below Marks Threshold', summary.studentsBelowMarksThreshold ?? '—', 'amber'],
        ['Flagged (either signal)', riskIds.size, 'violet']
      ].map(k => '<div class="kpi ' + k[2] + '"><div class="label">' + k[0] + '</div><div class="value">' + k[1] + '</div></div>').join('');

      if (!risky.length) {
        renderEmpty(tbody, 7, 'No students flagged', 'No one is below the attendance/marks thresholds right now.');
      } else {
        tbody.innerHTML = risky.map(s => {
          const p = perfById[s.studentId] || {};
          return '<tr data-id="' + s.studentId + '" data-roll="' + esc(s.rollNumber) + '">' +
            '<td><b>' + esc(fullName(s)) + '</b></td>' +
            '<td>' + esc(s.rollNumber) + '</td>' +
            '<td>' + esc(s.department ? s.department.departmentName : '—') + '</td>' +
            '<td>' + attBadge(p.attendancePercentage) + '</td>' +
            '<td>' + marksBadge(p.averageMarks) + '</td>' +
            '<td>' + signalBadges({ attendance: p.attendancePercentage, avgMarks: p.averageMarks }) + '</td>' +
            '<td class="actions"><button class="btn violet sm" data-act="predict">🤖 Analyze</button>' +
            '<button class="btn ghost sm" data-act="view">Profile</button></td></tr>';
        }).join('');
      }
    } catch (err) { renderError(tbody, 7, err, loadRiskList); }
  }

  async function predict(studentId) {
    const out = $('#aiOut');
    out.innerHTML = '<div class="state"><div class="spin"></div>Running model on real academic data…</div>';
    const result = await runAI($('#runBtn'), '/api/ai/students/' + studentId + '/insights', {},
      { target: out, retry: () => predict(studentId) });
    if (result) {
      out.innerHTML = '<div class="page-head"><div><h2 style="font-size:15px">Prediction result</h2></div></div>' +
        aiResultHTML(result, { showRecommendations: true });
    }
  }

  async function simulate(studentId) {
    const out = $('#simOut');
    if (!studentId) {
      out.innerHTML = '';
      toast('Select a student first, then run the simulation.', 'warn');
      return;
    }
    const body = {};
    if ($('#wG1').value !== '') body.g1 = Number($('#wG1').value);
    if ($('#wG2').value !== '') body.g2 = Number($('#wG2').value);
    if ($('#wStudy').value !== '') body.studyTime = Number($('#wStudy').value);
    if ($('#wAbs').value !== '') body.absences = Number($('#wAbs').value);
    if (!Object.keys(body).length) {
      out.innerHTML = '';
      toast('Enter at least one what-if value, or use the real-data prediction above.', 'warn');
      return;
    }
    if ((body.g1 === undefined) !== (body.g2 === undefined)) {
      out.innerHTML = '';
      toast('What-if requires both assessment scores together (or leave both empty).', 'warn');
      return;
    }

    out.innerHTML = '<div class="state"><div class="spin"></div>Running simulation…</div>';
    // current (real-data) + simulated, then show the comparison
    const [current, simulated] = await Promise.all([
      api('/api/ai/students/' + studentId + '/insights', { method: 'POST', body: '{}' }).catch(() => null),
      runAI($('#simBtn'), '/api/ai/students/' + studentId + '/simulate', body, { target: out, retry: () => simulate(studentId) })
    ]);
    if (simulated) {
      out.innerHTML = current
        ? aiComparisonHTML(current, simulated)
        : aiResultHTML(simulated);
    } else {
      out.innerHTML = ''; // runAI already rendered the error panel above
    }
  }

  $('#runBtn').onclick = () => {
    const id = $('#aiStudent').value;
    if (!id) { toast('Select a student first.', 'warn'); return; }
    predict(id);
  };

  $('#simBtn').onclick = () => {
    const id = $('#aiStudent').value;
    if (!id) { toast('Select a student first.', 'warn'); return; }
    simulate(id);
  };

  $('#riskRows').addEventListener('click', e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;
    if (btn.dataset.act === 'view') {
      sessionStorage.setItem('spis_selectedStudent', id);
      location.hash = '#student';
      return;
    }
    $('#aiStudent').value = id;
    $('#aiOut').scrollIntoView({ behavior: 'smooth' });
    predict(id);
  });

  await loadStudents();
  await loadRiskList();
  refreshAIHealth();
};
