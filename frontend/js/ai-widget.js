/* Shared AI insights widget — calls the real /api/ai endpoints.
   Renderers: aiResultHTML(result) + bindAIActions(root) + aiOfflineHTML() */

const AI_MODEL_DISCLAIMER =
  'AI predictions are estimates produced by a model trained on an external educational dataset ' +
  '(UCI Student Performance), applied to this student\'s real academic data. They should support ' +
  'faculty review, not replace academic judgment.';

function aiSufficiencyBadge(r) {
  const map = {
    SUFFICIENT_DATA: { cls: 'green', label: 'Sufficient data' },
    LIMITED_DATA: { cls: 'amber', label: 'Limited data' },
    INSUFFICIENT_DATA: { cls: 'red', label: 'Insufficient data' }
  };
  const m = map[r.dataSufficiency] || { cls: 'gray', label: 'Data quality unknown' };
  return '<span class="badge ' + m.cls + '">' + m.label + ' · ' + esc(r.dataQuality || '—') + '</span>';
}

function aiDataQualityHTML(r) {
  const basis = (r.dataBasis || []).map(b => '<li>' + esc(b) + '</li>').join('');
  const defaulted = (r.defaultedFields || []);
  const defaultedNote = defaulted.length
    ? '<div class="help mt">Values assumed (not tracked in this system): <b>' + defaulted.map(esc).join(', ') +
      '</b> — set to the training dataset\u2019s average level. Treat the prediction accordingly.</div>'
    : '';
  return '<div class="mt" style="background:var(--surface-2,#f8fafc);border:1px solid var(--border,#e2e8f0);border-radius:8px;padding:10px 12px">' +
    '<b style="font-size:12.5px">Based on</b>' +
    (basis ? '<ul style="margin:6px 0 0 18px;font-size:12.5px;color:var(--text-2,#475569);line-height:1.7">' + basis + '</ul>'
           : '<div class="help">No data basis returned.</div>') +
    defaultedNote +
    '</div>';
}

function aiFeaturesHTML(r) {
  const f = r.featuresUsed || {};
  const rows = [
    ['G1 (earlier assessment, 0-20 scale)', f.G1],
    ['G2 (recent assessment, 0-20 scale)', f.G2],
    ['Study time category', f.studytime],
    ['Past failures', f.failures],
    ['Absences', f.absences]
  ];
  return '<details class="mt"><summary class="muted" style="cursor:pointer;font-size:12.5px">Exact features sent to the model</summary>' +
    '<div class="table-wrap mt"><table class="data"><tbody>' +
    rows.map(x => '<tr><td>' + esc(x[0]) + '</td><td><b>' + esc(x[1] === undefined || x[1] === null ? '—' : x[1]) + '</b></td></tr>').join('') +
    '</tbody></table></div></details>';
}

function aiResultHTML(r, opts) {
  const o = opts || {};
  if (!r) return '<div class="state"><div class="big">🤖</div><div class="msg">No prediction yet</div></div>';

  const riskCls = r.riskLevel === 'HIGH' ? 'red' : r.riskLevel === 'MEDIUM' ? 'amber' : 'green';
  const catCls = r.performanceCategory === 'POOR' ? 'red' : r.performanceCategory === 'AVERAGE' ? 'amber' : 'green';

  return '' +
    '<div class="grid-2">' +
    '  <div class="card"><div class="card-body" style="text-align:center">' +
    '    <div class="muted">PREDICTED PERFORMANCE <span class="badge violet" style="margin-left:4px">Model estimate</span></div>' +
    '    <div style="font-size:34px;font-weight:800;margin-top:6px">' + (r.predictedScore != null ? Number(r.predictedScore).toFixed(1) : '—') + '<span class="muted" style="font-size:14px"> / 100</span></div>' +
    '    <div style="margin-top:6px"><span class="badge ' + catCls + '">' + esc(r.performanceCategory || '—') + '</span></div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body" style="text-align:center">' +
    '    <div class="muted">ACADEMIC RISK <span class="badge violet" style="margin-left:4px">Model estimate</span></div>' +
    '    <div style="font-size:34px;font-weight:800;margin-top:6px">' + (r.riskProbability != null ? (r.riskProbability * 100).toFixed(1) + '%' : '—') + '</div>' +
    '    <div style="margin-top:6px"><span class="badge ' + riskCls + '">' + esc(r.riskLevel || '—') + ' RISK</span></div>' +
    '  </div></div>' +
    '</div>' +

    '<div class="card mt"><div class="card-body">' +
    '  <h3 class="card-title">Data availability ' + aiSufficiencyBadge(r) + '</h3>' +
    aiDataQualityHTML(r) +
    '</div></div>' +

    '<div class="card mt"><div class="card-body">' +
    '  <h3 class="card-title">Factors considered by the model <span class="muted" style="font-weight:400">(comparisons, not causes)</span></h3>' +
    '  <ul style="margin-left:18px;font-size:13px;color:var(--text-2);line-height:1.7">' +
    '    ' + ((r.majorFactors || []).map(f => '<li>' + esc(f) + '</li>').join('') || '<li class="muted">No factor list returned by the model.</li>') +
    '  </ul>' +
    aiFeaturesHTML(r) +
    (o.showRecommendations ? aiRecommendationsHTML(r) : '') +
    '  <div class="help mt">🔒 ' + esc(AI_MODEL_DISCLAIMER) + '</div>' +
    '</div></div>';
}

/* What-if comparison view: current prediction vs simulated prediction. */
function aiComparisonHTML(current, simulated) {
  const fmt = v => (v != null ? Number(v).toFixed(1) : '—');
  const delta = (current.predictedScore != null && simulated.predictedScore != null)
    ? simulated.predictedScore - current.predictedScore : null;
  const deltaTxt = delta != null ? (delta >= 0 ? '+' : '') + delta.toFixed(1) : '—';
  const deltaCls = delta == null ? 'gray' : delta >= 0 ? 'green' : 'red';

  return '<div class="grid-3" style="grid-template-columns:1fr 1fr 1fr">' +
    '  <div class="card"><div class="card-body" style="text-align:center">' +
    '    <div class="muted">CURRENT PREDICTION</div>' +
    '    <div style="font-size:26px;font-weight:800;margin-top:4px">' + fmt(current.predictedScore) + '</div>' +
    '    <div class="help">from real academic data</div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body" style="text-align:center">' +
    '    <div class="muted">WHAT-IF PREDICTION</div>' +
    '    <div style="font-size:26px;font-weight:800;margin-top:4px">' + fmt(simulated.predictedScore) + '</div>' +
    '    <div class="help">simulated inputs only</div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body" style="text-align:center">' +
    '    <div class="muted">CHANGE</div>' +
    '    <div style="font-size:26px;font-weight:800;margin-top:4px"><span class="badge ' + deltaCls + '">' + deltaTxt + '</span></div>' +
    '    <div class="help">model prediction changed under these simulated inputs — not a causal effect</div>' +
    '  </div></div>' +
    '</div>' +
    '<div class="card mt"><div class="card-body">' +
    '  <h3 class="card-title">Simulation detail</h3>' +
    aiDataQualityHTML(simulated) + aiFeaturesHTML(simulated) +
    '  <div class="help mt">🔒 Simulation only — this does not modify the student\u2019s academic records.</div>' +
    '</div></div>';
}

/* Rule-based recommendations derived from the REAL prediction result values.
   Clearly labelled as generated guidance, not model output. */
function aiRecommendationsHTML(r) {
  const recs = [];
  if (r.riskLevel === 'HIGH') {
    recs.push('Schedule a one-on-one mentoring session this week.');
    recs.push('Verify attendance in every subject and address absences immediately.');
  } else if (r.riskLevel === 'MEDIUM') {
    recs.push('Monitor weekly test scores; arrange peer tutoring if the next score drops.');
  } else {
    recs.push('Performance is on track — maintain the current study routine.');
  }
  if (r.predictedScore != null && r.predictedScore < 50) {
    recs.push('Focus on foundational topics in the weakest subjects before the next exam.');
  }
  if ((r.majorFactors || []).some(f => /absen|attend/i.test(f))) {
    recs.push('Attendance appears among the model\u2019s factors: reinforce the minimum 75% target.');
  }
  if (r.dataSufficiency === 'LIMITED_DATA') {
    recs.push('Only limited history is available — re-run this analysis after the next assessment.');
  }
  return '<div class="mt" style="background:var(--primary-soft);border-radius:8px;padding:12px">' +
    '<b style="font-size:13px">Recommendations</b> <span class="badge blue" style="margin-left:4px">Generated guidance</span>' +
    '<ul style="margin:8px 0 0 18px;font-size:13px;color:var(--text-2);line-height:1.7">' +
    recs.map(x => '<li>' + esc(x) + '</li>').join('') + '</ul></div>';
}

/* Full-width offline panel with retry — used whenever the AI service is unreachable. */
function aiOfflineHTML(message, retryFn) {
  return '<div class="card"><div class="card-body"><div class="state error">' +
    '<div class="big">🤖</div><div class="msg">AI Service Offline</div>' +
    '<div class="sub">' + esc(message || 'The academic system is still fully usable, but AI predictions cannot be generated right now. Start the ML service (ml/app.py, port 5001) and retry.') + '</div>' +
    (retryFn ? '<button class="btn violet sm" id="aiRetryBtn">Retry</button>' : '') +
    '</div></div></div>';
}

/* Friendly reason for each known AI failure mode. Never hides the backend's
   own message — it is appended when present. */
function aiErrorSummary(e) {
  if (e && e.timeout) return 'The AI service did not respond in time. Check that the ML service (ml/app.py, port 5001) is running, then retry.';
  if (e && e.network) return 'The backend could not be reached. Check that Spring Boot is running on port 8080.';
  const s = e && e.status;
  if (s === 401) return 'Your session has expired. Please sign in again.';
  if (s === 403) return 'Your account does not have permission to run this prediction.';
  if (s === 404) return 'The student record needed for this prediction was not found.';
  if (s === 422) return 'There is not enough academic data for a reliable prediction. Record at least one assessment (marks entry) first.';
  if (s === 502) return 'The ML service rejected the prediction request. It answered, but could not process the data sent to it.';
  if (s === 503) return 'The AI inference service is not reachable. Start the ML service (ml/app.py, port 5001) and retry.';
  if (s === 500 || s === 504) return 'The backend hit an unexpected error while generating the prediction.';
  if (s) return 'The prediction request failed (HTTP ' + s + ').';
  return 'The prediction could not be completed.';
}

/* In-panel error card used for every failure mode — loading state is always
   replaced, never left spinning. Retry reruns the same prediction. */
function aiErrorPanelHTML(e, retryFn) {
  const detail = e && e.message ? String(e.message) : '';
  const known = e && (e.timeout || e.network || e.status);
  const offline = e && (e.status === 503 || e.network || e.timeout);
  return '<div class="card"><div class="card-body"><div class="state error">' +
    '<div class="big">' + (offline ? '🤖' : '⚠️') + '</div>' +
    '<div class="msg">' + (offline ? 'AI Service Offline' : 'AI Prediction Failed') + '</div>' +
    '<div class="sub">' + esc(aiErrorSummary(e)) +
    (detail && (!known || detail !== aiErrorSummary(e))
      ? '<br><span style="font-size:12px;opacity:.85">Reason: ' + esc(detail) + '</span>' : '') +
    '</div>' +
    (retryFn ? '<button class="btn violet sm" id="aiRetryBtn">Retry</button>' : '') +
    '</div></div></div>';
}

/* Shared POST for AI endpoints with distinct handling of every failure mode.
   The loading state in o.target is ALWAYS replaced on failure — with a
   readable in-panel error + [Retry] — so the UI can never stay stuck on
   "Running model…". 401 redirects to login inside api().

   Status semantics (from AiServiceExceptionHandler):
   - 503: ML service unreachable   → offline panel + retry
   - 502: ML service rejected data → its own message is preserved
   - 422: insufficient academic data → clear informational message
   - others (404/500/timeout/network/invalid JSON) → error panel + retry */
async function runAI(btn, url, body, opts) {
  const o = opts || {};
  const exec = async () => {
    let result = null;
    try {
      result = await api(url, { method: 'POST', body: JSON.stringify(body || {}) });
    } catch (e) {
      // 401 already redirects to login from api(); nothing else to render here.
      if (e && e.status === 401) return null;
      if (o.onError === 'toast') {
        toast(aiErrorSummary(e) + (e && e.message ? ' (' + e.message + ')' : ''), 'error', o.errorTitle || 'AI prediction failed');
      } else if (o.target) {
        o.target.innerHTML = aiErrorPanelHTML(e, o.retry);
        const rb = $('#aiRetryBtn', o.target);
        if (rb && o.retry) rb.onclick = () => { rb.disabled = true; o.retry(); };
      } else {
        toast(aiErrorSummary(e) + (e && e.message ? ' (' + e.message + ')' : ''), 'error', o.errorTitle || 'AI prediction failed');
      }
      return null;
    }
    // Backend answered 200 but with an unusable body — treat as an error,
    // not a success, otherwise the loading state would be replaced by nothing.
    if (!result || typeof result !== 'object' || result.predictedScore === undefined) {
      const err = new Error('The backend returned an unexpected response (no prediction payload).');
      if (o.target) {
        o.target.innerHTML = aiErrorPanelHTML(err, o.retry);
        const rb = $('#aiRetryBtn', o.target);
        if (rb && o.retry) rb.onclick = () => { rb.disabled = true; o.retry(); };
      } else {
        toast(aiErrorSummary(err), 'error', o.errorTitle || 'AI prediction failed');
      }
      return null;
    }
    return result;
  };
  return btn ? withBusy(btn, 'Predicting…', exec) : exec();
}

/* Health status pill: polls /api/ai/health at most once per 60s across views. */
let __aiHealthCache = { at: 0, status: null };
async function refreshAIHealth() {
  const el = $('#aiHealthPill');
  if (!el) return;
  const now = Date.now();
  if (now - __aiHealthCache.at > 60000) {
    __aiHealthCache.at = now;
    try {
      const h = await api('/api/ai/health');
      __aiHealthCache.status = h && h.status === 'UP' ? 'UP' : 'DOWN';
    } catch (e) {
      __aiHealthCache.status = 'DOWN';
    }
  }
  const up = __aiHealthCache.status === 'UP';
  el.innerHTML = (up ? '●' : '○') + ' AI ' + (up ? 'Online' : 'Offline');
  el.title = up ? 'ML inference service is running (port 5001).'
                : 'ML inference service is not reachable — predictions will fail until it is started.';
  el.style.color = up ? '#16a34a' : '#dc2626';
  el.style.cursor = 'default';
}
