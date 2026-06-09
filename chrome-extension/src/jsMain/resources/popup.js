const BASE_URL = "https://ow3icnkyfa.execute-api.ap-southeast-1.amazonaws.com/lambda_stage";

const isLocal = ['localhost', '127.0.0.1', '[::1]'].includes(window.location.hostname)

// Buttons
document.getElementById("hourlyBtn").addEventListener("click", () => loadData("hourly"));
document.getElementById("dailyBtn").addEventListener("click", () => loadData("daily"));

// Get current tab URL
async function getCurrentTabUrl() {
    if (isLocal) {
        return "https://localhost/tokopedia/123-1?q=1,2&p=1"
    }
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    return tab.url;
}

// Extract SKU ID from Tokopedia URL
function extractSkuId(url) {
    try {
        const path = new URL(url).pathname;
        const lastPart = path.split("/").pop();
        return lastPart.split("-").pop();
    } catch (e) {
        console.error("Invalid URL:", e);
        return null;
    }
}

// Load data from API
async function loadData(type) {
    const url = await getCurrentTabUrl();
    const skuId = extractSkuId(url);

    if (!skuId) {
        alert("SKU not found");
        return;
    }

    const endpoint = `${BASE_URL}/${type}/${skuId}`;

    if (isLocal) {
        if (type === "daily") {
            renderChart(type, dailyData)
        } else {
            renderChart(type, hourlyData)
        }
        return
    }

    try {
        const res = await fetch(endpoint);
        const data = await res.json();

        // Ensure chronological order (oldest → newest)
        renderChart(type, data.reverse());
    } catch (err) {
        console.error(err);
        alert("Failed to fetch data");
    }
}

// Render chart
function renderChart(type, data) {
    const chart = new Chart(
        document.getElementById("chart-" + type).getContext("2d"),
        {
            type: "line",
            data: {
                labels: data.map(d => new Date(d.instant)),
                datasets: [
                    {
                        label: "Min",
                        data: data.map(d => d.min),
                        borderColor: "rgba(0,180,0,0.6)",
                        fill: "+2",
                        tension: 0.2
                    },
                    {
                        label: "Mean",
                        data: data.map(d => d.mean),
                        borderColor: "blue",
                        borderWidth: 2,
                        fill: false,
                        tension: 0.2
                    },
                    {
                        label: "Max",
                        data: data.map(d => d.max),
                        borderColor: "rgba(255,0,0,0.6)",
                        fill: false,
                        tension: 0.2
                    },
                ]
            },
            options: {
                responsive: true,
                interaction: {
                    mode: "index",
                    intersect: false
                },
                plugins: {
                    legend: {
                        display: true
                    }
                },
                scales: {
                    x: {
                        type: 'time',
                        time: {
                            unit: type === 'hourly' ? 'hour' : 'day',
                            displayFormats: {
                                hour: 'k',
                                day: 'd',
                            }
                        },
                        ticks: {major: { enabled: true }}
                    }
                }
            }
        },
    );

    return () => chart.destroy()
}

// Auto-load hourly on popup open
loadData("daily");
loadData("hourly");

const dailyData = [
    { "skuId": "SKU-001", "mean": 52.3, "max": 78.1, "min": 21.4, "instant": 1704067200000 },
    { "skuId": "SKU-001", "mean": 49.8, "max": 75.0, "min": 20.2, "instant": 1704153600000 },
    { "skuId": "SKU-001", "mean": 55.1, "max": 80.3, "min": 25.0, "instant": 1704240000000 },
    { "skuId": "SKU-001", "mean": 60.4, "max": 88.9, "min": 30.1, "instant": 1704412800000 },
    { "skuId": "SKU-001", "mean": 47.2, "max": 70.5, "min": 18.9, "instant": 1704499200000 },
    { "skuId": "SKU-001", "mean": 50.0, "max": 72.3, "min": 22.0, "instant": 1704585600000 },
    { "skuId": "SKU-001", "mean": 58.6, "max": 85.2, "min": 27.3, "instant": 1704672000000 },
    { "skuId": "SKU-001", "mean": 62.1, "max": 90.0, "min": 31.5, "instant": 1704844800000 },
    { "skuId": "SKU-001", "mean": 53.9, "max": 79.8, "min": 24.7, "instant": 1704931200000 },
    { "skuId": "SKU-001", "mean": 48.5, "max": 73.2, "min": 19.3, "instant": 1705017600000 },

    { "skuId": "SKU-001", "mean": 51.2, "max": 76.4, "min": 23.1, "instant": 1705104000000 },
    { "skuId": "SKU-001", "mean": 57.7, "max": 83.5, "min": 26.8, "instant": 1705276800000 },
    { "skuId": "SKU-001", "mean": 61.5, "max": 89.2, "min": 32.4, "instant": 1705363200000 },
    { "skuId": "SKU-001", "mean": 46.9, "max": 68.0, "min": 17.5, "instant": 1705449600000 },
    { "skuId": "SKU-001", "mean": 49.3, "max": 71.1, "min": 20.6, "instant": 1705536000000 },
    { "skuId": "SKU-001", "mean": 54.8, "max": 82.0, "min": 25.9, "instant": 1705622400000 },
    { "skuId": "SKU-001", "mean": 59.2, "max": 87.6, "min": 29.0, "instant": 1705795200000 },
    { "skuId": "SKU-001", "mean": 63.0, "max": 91.4, "min": 33.2, "instant": 1705881600000 },
    { "skuId": "SKU-001", "mean": 52.7, "max": 78.6, "min": 23.8, "instant": 1705968000000 },
    { "skuId": "SKU-001", "mean": 47.8, "max": 69.9, "min": 18.1, "instant": 1706054400000 },

    { "skuId": "SKU-001", "mean": 50.6, "max": 74.3, "min": 21.9, "instant": 1706140800000 },
    { "skuId": "SKU-001", "mean": 56.9, "max": 84.1, "min": 27.2, "instant": 1706313600000 },
    { "skuId": "SKU-001", "mean": 60.8, "max": 88.7, "min": 31.6, "instant": 1706400000000 },
    { "skuId": "SKU-001", "mean": 45.5, "max": 67.4, "min": 16.8, "instant": 1706486400000 },
    { "skuId": "SKU-001", "mean": 48.9, "max": 72.5, "min": 19.7, "instant": 1706572800000 },
    { "skuId": "SKU-001", "mean": 55.4, "max": 81.9, "min": 26.3, "instant": 1706659200000 },
    { "skuId": "SKU-001", "mean": 58.1, "max": 86.0, "min": 28.5, "instant": 1706832000000 },
    { "skuId": "SKU-001", "mean": 62.7, "max": 92.3, "min": 34.1, "instant": 1706918400000 },
    { "skuId": "SKU-001", "mean": 51.9, "max": 77.0, "min": 22.8, "instant": 1707004800000 },
    { "skuId": "SKU-001", "mean": 46.3, "max": 68.7, "min": 17.2, "instant": 1707091200000 },

    { "skuId": "SKU-001", "mean": 49.7, "max": 73.8, "min": 20.1, "instant": 1707177600000 },
    { "skuId": "SKU-001", "mean": 57.2, "max": 83.9, "min": 27.9, "instant": 1707350400000 },
    { "skuId": "SKU-001", "mean": 61.0, "max": 89.5, "min": 32.0, "instant": 1707436800000 },
    { "skuId": "SKU-001", "mean": 44.8, "max": 66.2, "min": 15.9, "instant": 1707523200000 },
    { "skuId": "SKU-001", "mean": 48.0, "max": 71.0, "min": 19.0, "instant": 1707609600000 },
    { "skuId": "SKU-001", "mean": 54.2, "max": 80.8, "min": 25.4, "instant": 1707696000000 },
    { "skuId": "SKU-001", "mean": 59.9, "max": 87.1, "min": 29.9, "instant": 1707868800000 },
    { "skuId": "SKU-001", "mean": 63.5, "max": 93.0, "min": 35.0, "instant": 1707955200000 },
    { "skuId": "SKU-001", "mean": 52.1, "max": 78.2, "min": 23.3, "instant": 1708041600000 },
    { "skuId": "SKU-001", "mean": 47.0, "max": 69.1, "min": 18.4, "instant": 1708128000000 },

    { "skuId": "SKU-001", "mean": 50.3, "max": 75.2, "min": 21.7, "instant": 1708214400000 },
    { "skuId": "SKU-001", "mean": 56.5, "max": 82.7, "min": 26.6, "instant": 1708387200000 },
    { "skuId": "SKU-001", "mean": 60.2, "max": 88.0, "min": 30.8, "instant": 1708473600000 },
    { "skuId": "SKU-001", "mean": 45.9, "max": 67.9, "min": 16.5, "instant": 1708560000000 },
    { "skuId": "SKU-001", "mean": 49.1, "max": 72.0, "min": 20.0, "instant": 1708646400000 },
    { "skuId": "SKU-001", "mean": 55.9, "max": 81.5, "min": 26.9, "instant": 1708732800000 },
    { "skuId": "SKU-001", "mean": 58.8, "max": 85.7, "min": 28.8, "instant": 1708905600000 },
    { "skuId": "SKU-001", "mean": 62.4, "max": 91.0, "min": 33.7, "instant": 1708992000000 },
    { "skuId": "SKU-001", "mean": 53.0, "max": 79.4, "min": 24.0, "instant": 1709078400000 },
    { "skuId": "SKU-001", "mean": 46.7, "max": 68.5, "min": 17.9, "instant": 1709164800000 }
]

const hourlyData = [
    {
        skuId: "1",
        price: 1000000,
        instant: "2025-12-31T01:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1100000,
        instant: "2026-01-01T06:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-01T07:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-02T10:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-02T10:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-02T12:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-02T13:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-02T15:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-02T17:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-02T18:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1040000,
        instant: "2026-01-04T01:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-04T02:00:00.000Z",
    },
    {
        skuId: "1",
        price: 1050000,
        instant: "2026-01-04T04:00:00.000Z",
    },
]