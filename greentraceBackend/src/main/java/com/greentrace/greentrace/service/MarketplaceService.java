package com.greentrace.greentrace.service;

import com.greentrace.greentrace.model.MarketplaceListing;
import com.greentrace.greentrace.repository.MarketplaceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketplaceService {

    @Autowired
    private MarketplaceRepository marketplaceRepository;

    public MarketplaceListing listCredits(MarketplaceListing listing){
        listing.setStatus("AVAILABLE");
        return marketplaceRepository.save(listing);
    }

    public List<MarketplaceListing> getListings(){
        return marketplaceRepository.findAll();
    }

    public MarketplaceListing buyCredits(String id){

        MarketplaceListing listing =
                marketplaceRepository.findById(id).orElse(null);

        if(listing != null){
            listing.setStatus("SOLD");
            return marketplaceRepository.save(listing);
        }

        return null;
    }
}