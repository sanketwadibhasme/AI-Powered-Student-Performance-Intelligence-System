/* Rankings module — student / subject / department / attendance rankings */
window.view_rankings = async function (view) {
  const role = getRole();
  const tabs = [
    ['students', 'Student Rankings'],
    ['subjects', 'Subject Rankings'],
    ['departments', 'Department Rankings'],
    ['attendance', 'Attendance Rankings']
  ];

  view.innerHTML =
    '<div class="page-head"><div><h2>Rankings</h2><div class="desc">Ranked by backend-computed averages — no custom scoring.</div></div>' +
    '<div class="page-actions"><button class="btn ghost" id="exportBtn">⬇ Export</button></div></div>' +
    '<div class="tabs">' + tabs.map((t, i) => '<button data-tab="' + t[0] + '" ' + (i === 0 ? 'class="active"' : '') + '>' + t[1] + '</button>').join('') + '</div>' +
    '<div class="card"><div class="card-body"><div id="rankBody"><div class="state"><div class="spin"></div>Loading…</div></div></div></div>';

  let currentExport = null;

  $all('.tabs button').forEach(b => b.addEventListener('click', () => {
    $all('.tabs button').forEach(x => x.classList.remove('active'));
    b.classList.add('active');
    load(b.dataset.tab);
  }));

  function tableHTML(head, rowsBuilder) {
    return '<div class="table-wrap"><table class="data"><thead><tr>' +
      '<th style="width:70px">Rank</th>' + head + '</tr></thead><tbody>' + rowsBuilder + '</tbody></table></div>';
  }
  const medal = r => r <= 3 ? ['🥇', '🥈', '🥉'][r - 1] : '#' + r;

  async function load(tab) {
    const body = $('#rankBody');
    body.innerHTML = '<div class="state"><div class="spin"></div>Loading…</div>';
    try {
      if (tab === 'students') {
        const list = await api('/api/reports/student-ranking');
        body.innerHTML = list.length ? tableHTML('<th>Roll No</th><th>Student</th><th>Department</th><th>Average Marks</th>',
          list.map(r =>
            '<tr' + (role === 'STUDENT' && r.studentId === Number(sessionStorage.getItem('spis_studentId')) ? ' style="background:var(--primary-soft)"' : '') + '>' +
            '<td><b>' + medal(r.rank) + '</b></td><td>' + esc(r.rollNumber) + '</td><td>' + esc(r.studentName) + '</td>' +
            '<td>' + esc(r.department) + '</td><td>' + marksBadge(r.averageMarks) + '</td></tr>').join('')
        ) : '<div class="state"><div class="big">📭</div><div class="msg">No ranking data yet</div><div class="sub">Add students and marks to generate rankings.</div></div>';
        currentExport = {
          name: 'student-ranking',
          columns: [{ label: 'Rank', value: 'rank' }, { label: 'Roll No', value: 'roll' }, { label: 'Student', value: 'name' }, { label: 'Department', value: 'dept' }, { label: 'Average Marks', value: 'marks' }],
          rows: list.map(r => ({ rank: r.rank, roll: r.rollNumber, name: r.studentName, dept: r.department, marks: r.averageMarks }))
        };
      } else if (tab === 'subjects') {
        const list = await api('/api/reports/subject-ranking');
        body.innerHTML = list.length ? tableHTML('<th>Code</th><th>Subject</th><th>Department</th><th>Average Marks</th>',
          list.map(r =>
            '<tr><td><b>' + medal(r.rank) + '</b></td><td>' + esc(r.subjectCode) + '</td><td>' + esc(r.subjectName) + '</td>' +
            '<td>' + esc(r.department) + '</td><td>' + marksBadge(r.averageMarks) + '</td></tr>').join('')
        ) : '';
        currentExport = {
          name: 'subject-ranking',
          columns: [{ label: 'Rank', value: 'rank' }, { label: 'Code', value: 'code' }, { label: 'Subject', value: 'name' }, { label: 'Department', value: 'dept' }, { label: 'Average Marks', value: 'marks' }],
          rows: list.map(r => ({ rank: r.rank, code: r.subjectCode, name: r.subjectName, dept: r.department, marks: r.averageMarks }))
        };
      } else if (tab === 'departments') {
        const list = await api('/api/reports/department-ranking');
        body.innerHTML = list.length ? tableHTML('<th>Department</th><th>Average Marks</th>',
          list.map(r =>
            '<tr><td><b>' + medal(r.rank) + '</b></td><td>' + esc(r.departmentName) + '</td><td>' + marksBadge(r.averageMarks) + '</td></tr>').join('')
        ) : '';
        currentExport = {
          name: 'department-ranking',
          columns: [{ label: 'Rank', value: 'rank' }, { label: 'Department', value: 'name' }, { label: 'Average Marks', value: 'marks' }],
          rows: list.map(r => ({ rank: r.rank, name: r.departmentName, marks: r.averageMarks }))
        };
      } else {
        const list = await api('/api/reports/lowest-attendance');
        body.innerHTML = list.length ? tableHTML('<th>Roll No</th><th>Student</th><th>Department</th><th>Attendance %</th>',
          list.map(r =>
            '<tr><td><b>' + medal(r.rank) + '</b></td><td>' + esc(r.rollNumber) + '</td><td>' + esc(r.studentName) + '</td>' +
            '<td>' + esc(r.department) + '</td><td>' + attBadge(r.attendancePercentage) + '</td></tr>').join('')
        ) : '';
        body.insertAdjacentHTML('beforeend', '<div class="table-note muted">Showing lowest attendance first. Highest attendance is available via Reports.</div>');
        currentExport = {
          name: 'attendance-ranking',
          columns: [{ label: 'Rank', value: 'rank' }, { label: 'Roll No', value: 'roll' }, { label: 'Student', value: 'name' }, { label: 'Department', value: 'dept' }, { label: 'Attendance %', value: 'att' }],
          rows: list.map(r => ({ rank: r.rank, roll: r.rollNumber, name: r.studentName, dept: r.department, att: Number(r.attendancePercentage.toFixed(1)) }))
        };
      }
    } catch (err) {
      body.innerHTML = '<div class="state error"><div class="big">⚠️</div><div class="msg">' + esc(err.message) + '</div>' +
        '<button class="btn ghost sm" onclick="location.reload()">Retry</button></div>';
    }
  }

  $('#exportBtn').onclick = async () => {
    if (!currentExport) { toast('Load a ranking first.', 'warn'); return; }
    await exportWithPicker(currentExport.name, currentExport.columns, currentExport.rows);
  };

  await load('students');
};
