import Queue from "./queue.js"

const DRY_RUN = true
const INTERVAL_MS = DRY_RUN ? 1000 : 10 * 60 * 1000
const BATCH_SIZE = 100

async function removeOldItemsPromise() {
    const allKeys = await chrome.storage.local.getKeys()
    const oldKeys = allKeys.filter(key => key.split('.')[1] < Date.now() - 60 * 60 * 1000)
    await chrome.storage.local.remove(oldKeys)
}

async function enqueueNotSentPrices() {
    const keys = await chrome.storage.local.getKeys()
    const items = await chrome.storage.local.get(keys)
    queue.enqueue(...Object.entries(items)
        .filter(([, { sent }]) => !sent)
        .map(([id, { initial, max, min }]) => {
            const [skuId, instant] = id.split('.');
            return {
                skuId,
                instant,
                initial,
                max,
                min
            }
        })
    );
}

(async () => {
    await removeOldItemsPromise()
    await enqueueNotSentPrices()
})();

const queue = new Queue(async (...items) => {
    const response = DRY_RUN ? { ok: true } : await fetch("https://ow3icnkyfa.execute-api.ap-southeast-1.amazonaws.com/hourly", {
        headers: {
            "content-type": "application/json",
        },
        body: JSON.stringify(items),
        "method": "POST",
    });
    if (response.ok) {
        const updatedItems = {};
        for (const {skuId, instant, initial, max, min} of items) {
            updatedItems[`${skuId}.${instant}`] = {
                skuId,
                instant,
                initial,
                max,
                min,
                sent: true
            };
        }
        await chrome.storage.local.set(updatedItems);
    } else {
        throw new Error(`${response.status} ${response.statusText}: ${response.body}`);
    }
}, INTERVAL_MS, BATCH_SIZE);
queue.start();

chrome.storage.local.onChanged.addListener((changedItems) => {
    queue.enqueue(...Object.entries(changedItems)
        .filter(([, { newValue }]) => newValue && !newValue.sent)
        .map(([id, { newValue: { initial, max, min } }]) => {
            const [skuId, instant] = id.split('.');
            return {
                skuId,
                instant,
                initial,
                max,
                min
            };
        })
    );
});