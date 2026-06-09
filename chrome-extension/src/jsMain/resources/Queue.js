export default class Queue {
    constructor(processFn, intervalMs = 1000, batchSize = 1) {
        this.queue = [];
        this.intervalMs = intervalMs;
        this.batchSize = batchSize;
        this.processFn = processFn;
        this.timer = null;
        this.isProcessing = false;
    }

    enqueue(...items) {
        this.queue.push(...items);
    }

    start() {
        if (this.timer) return;

        this.timer = setInterval(async () => {
            if (this.isProcessing) return;
            if (this.queue.length === 0) return;

            this.isProcessing = true;

            const items = this.queue.slice(0, this.batchSize);
            console.log('Processing:', items);

            try {
                const result = await this.processFn(...items);
                this.queue.splice(0, this.batchSize);
                console.log('Processed:', result);
            } catch (e) {
                console.error('Failed to process:', e);
                this.stop();
            } finally {
                this.isProcessing = false;
            }
        }, this.intervalMs);
    }

    stop() {
        clearInterval(this.timer);
        this.timer = null;
    }
}
