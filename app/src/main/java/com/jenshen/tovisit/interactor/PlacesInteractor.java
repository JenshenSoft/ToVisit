package com.jenshen.tovisit.interactor;


import android.location.Location;

import com.jenshen.tovisit.api.entity.NearByResponse;
import com.jenshen.tovisit.api.entity.place.Place;
import com.jenshen.tovisit.manager.api.IApiManager;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class PlacesInteractor {

    private final IApiManager apiManager;

    @Inject
    public PlacesInteractor(IApiManager apiManager) {
        this.apiManager = apiManager;
    }
    
    public Observable<List<Place>> getPlaces(Location location) {
        return apiManager.getPlaces(location.getLatitude(), location.getLongitude(), null, null)
                .map(NearByResponse::getPlaces)
                .observeOn(Schedulers.computation())
                .flatMap(Observable::fromIterable)
                .toList();
    }
}
