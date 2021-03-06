/*
 *  Copyright 2015 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jenshen.tovisit.base.presenter;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby.mvp.MvpPresenter;
import com.hannesdorfmann.mosby.mvp.lce.MvpLceView;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.subscribers.single.ConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;


public abstract class MvpLceRxPresenter<M, V extends MvpLceView<M>>
        extends MvpBasePresenter<V>
        implements MvpPresenter<V> {

    private CompositeDisposable compositeDisposable;

    /**
     * Unsubscribes the observers and set it to null
     */
    protected void unsubscribe() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
            compositeDisposable = null;
        }
    }

    /**
     * Subscribes the presenter himself as observer on the observable
     *
     * @param observable    The observable to subscribeOnModel
     * @param pullToRefresh Pull to refresh?
     */
    protected void subscribeOnModel(Scheduler scheduler, Observable<M> observable, final boolean pullToRefresh) {
        Observer<M> observer = new Observer<M>() {

            private Disposable disposable;

            @Override
            public void onComplete() {
                compositeDisposable.remove(disposable);
                MvpLceRxPresenter.this.onComplete();
            }

            @Override
            public void onError(Throwable e) {
                compositeDisposable.remove(disposable);
                MvpLceRxPresenter.this.onError(e, pullToRefresh);
            }

            @Override
            public void onSubscribe(Disposable disposable) {
                this.disposable = disposable;
                compositeDisposable.add(disposable);
                MvpLceRxPresenter.this.onSubscribe(pullToRefresh);
            }

            @Override
            public void onNext(M m) {
                MvpLceRxPresenter.this.onNext(m);
            }
        };

        observable.subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * Subscribes the presenter himself as observer on the observable
     *
     * @param observable    The observable to subscribe
     * @param pullToRefresh Pull to refresh?
     */
    protected void subscribeOnModel(Observable<M> observable, final boolean pullToRefresh) {
        subscribeOnModel(Schedulers.io(), observable, pullToRefresh);
    }

    @SuppressWarnings("ConstantConditions")
    protected void onSubscribe(final boolean pullToRefresh) {
        if (isViewAttached()) {
            getView().showLoading(pullToRefresh);
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected void onComplete() {
        if (isViewAttached()) {
            getView().showContent();
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected void onError(Throwable e, boolean pullToRefresh) {
        e.printStackTrace();
        if (isViewAttached()) {
            getView().showError(e, pullToRefresh);
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected void onNext(M data) {
        if (isViewAttached()) {
            getView().setData(data);
        }
    }

    /**
     * Subscribes the presenter himself as observer on the observable
     *
     * @param observable The observable to subscribe
     */

    protected <T> void subscribe(Single<T> observable, ConsumerSingleObserver<T> observer) {
        subscribe(Schedulers.io(), observable, observer);
    }

    /**
     * Subscribes the presenter himself as observer on the observable
     *
     * @param observable The observable to subscribe
     */

    protected <T> void subscribe(Scheduler scheduler, Single<T> observable, ConsumerSingleObserver<T> observer) {
        compositeDisposable.add(observable.subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observer));
    }

    @Override
    public void attachView(V view) {
        super.attachView(view);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (!retainInstance) {
            unsubscribe();
        }
    }
}
