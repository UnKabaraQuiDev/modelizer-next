const root = document.documentElement;
const themeButton = document.getElementById('themeButton');
const themeLabel = themeButton.querySelector('.theme-label');
const savedTheme = localStorage.getItem('theme');
let downloadMetadata = null;
if (savedTheme) root.dataset.theme = savedTheme;

function syncTheme() {
  const dark = root.dataset.theme === 'dark';
  themeLabel.textContent = dark ? 'Dark' : 'Light';
  document.querySelector('meta[name="theme-color"]').setAttribute('content', dark ? '#08111f' : '#f8fafc');
}
themeButton.addEventListener('click', () => {
  root.dataset.theme = root.dataset.theme === 'dark' ? 'light' : 'dark';
  localStorage.setItem('theme', root.dataset.theme);
  syncTheme();
});

function selectedButton(groupId) {
  return document.querySelector(`#${groupId} .choice[aria-pressed="true"]`);
}

function selectedKey(groupId) {
  return selectedButton(groupId)?.dataset.key || "";
}

function setupChoices(groupId, outputId) {
  const group = document.getElementById(groupId);
  const output = document.getElementById(outputId);
  const initial = selectedButton(groupId);
  if (initial) {
    output.textContent = initial.dataset.value;
    output.dataset.key = initial.dataset.key;
  }
  group.addEventListener('click', event => {
    const button = event.target.closest('.choice');
    if (!button) return;
    group.querySelectorAll('.choice').forEach(choice => choice.setAttribute('aria-pressed', 'false'));
    button.setAttribute('aria-pressed', 'true');
    output.textContent = button.dataset.value;
    output.dataset.key = button.dataset.key;
    updateDownloadFromMetadata();
  });
}
async function loadDownloadMetadata() {
  try {
    const response = await fetch('https://raw.githubusercontent.com/UnKabaraQuiDev/modelizer-next/refs/heads/pages/metadata.json', {
      cache: 'no-store'
    });
    if (!response.ok) throw new Error(`metadata.json returned ${response.status}`);
    downloadMetadata = await response.json();
  } catch (error) {
    downloadMetadata = null;
  }
  updateDownloadFromMetadata();
} 

function findAsset(osKey, buildKey) {
  if (!downloadMetadata || !downloadMetadata.assets) return null;
  return downloadMetadata.assets?.[osKey]?.[buildKey] || null;
}

function setDownloadUnavailable(message) {
  const button = document.getElementById('downloadButton');
  document.getElementById('selectedHint').textContent = message;
  document.getElementById('selectedFile').textContent = 'Not available, please download manually below.';
  button.href = 'https://github.com/UnKabaraQuiDev/modelizer-next/releases';
  button.textContent = 'Open releases';
}

function updateDownloadFromMetadata() {
  const osKey = selectedKey('osChoices');
  const buildKey = selectedKey('buildChoices');
  const asset = findAsset(osKey, buildKey);
  if (!asset) {
    setDownloadUnavailable(downloadMetadata ? 'No matching file found in metadata.json.' : 'Could not load metadata.json.');
    return;
  }
  document.getElementById('selectedHint').textContent = asset.label || `${document.getElementById('selectedOs').textContent} ${document.getElementById('selectedBuild').textContent}`;
  document.getElementById('selectedFile').textContent = asset.file || asset.url || 'Download file';
  const button = document.getElementById('downloadButton');
  button.href = asset.url;
  button.textContent = 'Download selected build';
}
setupChoices('osChoices', 'selectedOs');
setupChoices('buildChoices', 'selectedBuild');
document.getElementById('year').textContent = new Date().getFullYear();
syncTheme();
loadDownloadMetadata();