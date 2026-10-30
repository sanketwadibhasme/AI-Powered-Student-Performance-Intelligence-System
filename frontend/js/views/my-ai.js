/* My AI Insights — student self-service (JWT-resolved, own data only) */
window.view_myai = async function (view) {
  view.innerHTML =
    '<div class="page-head"><div><h2>My AI Insights</h2>' +
    '<div class="desc">The model uses your real marks and attendance from the system. <span id="aiHealthPill" class="badge gray" style="margin-left:6px;cursor:default">AI …</span></div></div></div>' +

    '<div id="aiOut"></div>' +

    '<div class="card mb"><div class="card-body">' +
    '  <h3 class="card-title">Your actual data (input to the model)</h3><div id="actuals" class="muted">Loading…</div>' +
    '</div></div>' +

    '<div class="card mb"><div class="card-body">' +
    '  <h3 class="card-title">What-if exploration <span class="badge gray" style="margin-left:6px">Simulation only</span></h3>' +
    '  <div class="help mb">Explore a hypothetical scenario. What-if values only affect the prediction — they never change your records, and the difference shown is a model estimate, not a real outcome.</div>' +
    '  <div class="form-grid">' +
    '    <div class="field"><label>First assessment score (0-100)</label><input id="wG1" type="number" min="0" max="100"></div>' +
    '    <div class="field"><label>Recent assessment score (0-100)</label><input id="wG2" type="number" min="0" max="100"></div>' +
    '    <div class="field"><label>Weekly study time (1 &lt;2h · 2: 2-5h · 3: 5-10h · 4: &gt;10h)</label>' +
    '      <select id="wStudy"><option value="">leave as is</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select></div>' +
    '    <div class="field"><label>Absences</label><input id="wAbs" type="number" min="0"></div>' +
    '  </div>' +
    '  <button class="btn ghost mt" id="simBtn">▶ Run Simulation</button>' +
    '  <div id="simOut" class="mt"></div>' +
    '</div></div>';

  /* real-data prediction first */
  const out = $('#aiOut');
  out.innerHTML = '<div class="state"><div class="spin"></div>Running the model on your real academic data…</div>';
  const result = await runAI(null, '/api/ai/me/insights', {}, { target: out, retry: runReal });
  if (result) out.innerHTML = aiResultHTML(result, { showRecommendations: true });

  function runReal() {
    view_myai(view);
  }

  /* actuals panel */
  try {
    const [avg, att] = await Promise.all([
      api('/api/students/me/average-marks'),
      api('/api/students/me/attendance-percentage')
    ]);
    $('#actuals').innerHTML =
      '<div class="flex" style="gap:26px">' +
      '  <div><div class="muted">Average marks</div><b style="font-size:20px">' + (avg != null ? num(avg) : '—') + '</b></div>' +
      '  <div><div class="muted">Attendance</div><b style="font-size:20px">' + (att != null ? pct(att) : '—') + '</b></div>' +
      '</div><div class="help mt">Historical values stored in the system. The model\u2019s G1/G2 are your two most recent assessments by exam date; absences come from your attendance records.</div>';
  } catch (e) { $('#actuals').textContent = 'Unable to load your actuals: ' + e.message; }

  $('#simBtn').onclick = async () => {
    const body = {};
    if ($('#wG1').value !== '') body.g1 = Number($('#wG1').value);
    if ($('#wG2').value !== '') body.g2 = Number($('#wG2').value);
    if ($('#wStudy').value !== '') body.studyTime = Number($('#wStudy').value);
    if ($('#wAbs').value !== '') body.absences = Number($('#wAbs').value);
    if (!Object.keys(body).length) { toast('Enter at least one what-if value to simulate.', 'warn'); return; }
    if ((body.g1 === undefined) !== (body.g2 === undefined)) {
      toast('What-if requires both assessment scores together (or leave both empty).', 'warn'); return;
    }

    const simOut = $('#simOut');
    simOut.innerHTML = '<div class="state"><div class="spin"></div>Running simulation…</div>';
    const [current, simulated] = await Promise.all([
      result, // the real-data prediction from page load
      runAI($('#simBtn'), '/api/ai/me/insights', body, { target: simOut, retry: () => $('#simBtn').click() })
    ]);
    if (simulated) {
      simOut.innerHTML = current ? aiComparisonHTML(current, simulated) : aiResultHTML(simulated);
    }
  };

  refreshAIHealth();
};
