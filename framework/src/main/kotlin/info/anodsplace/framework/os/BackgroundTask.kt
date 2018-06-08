package info.anodsplace.framework.os

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.util.LruCache
import info.anodsplace.framework.app.ApplicationContext

/**
 * @author Alex Gavrishev
 * *
 * @date 14/04/2017.
 */

class CachedBackgroundTask<P, R>(private val key: String, private val worker: BackgroundTask.Worker<P, R>, private val storage: LruCache<String, Any?>) {

    constructor(key: String, worker: BackgroundTask.Worker<P, R>, context: ApplicationContext)
        : this(key, worker, context.memoryCache)

    fun execute() {
        val cached = storage.get(key) as? R
        if (cached != null) {
            worker.finished(cached)
            return
        }

        BackgroundTask(object : BackgroundTask.Worker<P, R>(this.worker.param) {
            override fun run(param: P): R {
                return worker.run(param)
            }

            override fun finished(result: R) {
                storage.put(key, result)
                worker.finished(result)
            }
        }).execute()
    }
}

class BackgroundTask<P, R>(private val worker: Worker<P, R>) : AsyncTask<Void, Void, R>() {

    abstract class Worker<Param, Result> protected constructor(internal val param: Param) {
        abstract fun run(param: Param): Result
        abstract fun finished(result: Result)
    }

    override fun doInBackground(vararg params: Void): R {
        return this.worker.run(this.worker.param)
    }

    override fun onPostExecute(result: R) {
        this.worker.finished(result)
    }
}

class LiveDataTask<P, R>(private val worker: LiveDataTask.Worker<P, R>) : AsyncTask<P, Void, R>() {
    private val liveData = MutableLiveData<R>()

    abstract class Worker<Param, out Result> protected constructor(internal val param: Param) {
        abstract fun run(param: Param): Result
    }

    override fun doInBackground(vararg params: P): R {
        return this.worker.run(this.worker.param)
    }

    override fun onPostExecute(result: R) {
        liveData.value = result
    }

    fun execute(): LiveData<R> {
        super.execute()
        return liveData
    }
}