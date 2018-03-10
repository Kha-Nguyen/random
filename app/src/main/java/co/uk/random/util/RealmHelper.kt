package co.uk.random.util

import android.util.Log
import co.uk.random.model.Playlist
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery


object RealmHelper {

    fun copyOrUpdate(realmObject: RealmObject) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(realmObject)
        realm.commitTransaction()
        realm.close()
    }

    inline fun <reified T : RealmObject> copyOrUpdateAutoIncrement(realmObject: RealmObject, clazz: Class<T>) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val maxId = it.where(realmObject::class.java)?.max("id")
            val nextId = if (maxId == null) 1 else maxId.toInt() + 1
            val clazzOne = it.createObject(clazz, nextId)
            clazzOne.realm.copyToRealmOrUpdate(clazzOne)
        }
    }

    inline fun <reified T : RealmObject> findAll(): Single<List<T>> = findAllBy()

    inline fun <reified T : RealmObject> findAllBy(query: (RealmQuery<T>) -> RealmQuery<T> = { it }): Single<List<T>> {
        val realm = Realm.getDefaultInstance()
        return Single.just(query(realm.where(T::class.java)).findAll())
                .flatMap {
                    val results = ArrayList<T>()
                    it.forEach { results.add(it) }
                    return@flatMap Single.just(results)
                }
    }

    fun delete(clazz: Class<out RealmObject>) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.where(clazz).findAll().deleteAllFromRealm()
        realm.commitTransaction()
        realm.close()
    }

    fun clearAllCache() {
        Realm.getDefaultInstance()
                .executeTransaction { realm -> realm.deleteAll() }
    }

    private fun getPlaylistFromRealm(): Single<List<Playlist>> =
            RealmHelper.findAll<Playlist>().flatMap {
                return@flatMap Single.just(it)
            }
}