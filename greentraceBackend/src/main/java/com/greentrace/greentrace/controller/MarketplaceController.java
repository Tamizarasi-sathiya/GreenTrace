package com.greentrace.greentrace.controller;

import com.greentrace.greentrace.model.MarketplaceListing;
import com.greentrace.greentrace.service.MarketplaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@CrossOrigin
public class MarketplaceController {

    @Autowired
    private MarketplaceService marketplaceService;

    // =========================
    // LIST CARBON CREDITS
    // =========================
    @PostMapping("/list")
    public MarketplaceListing listCredits(@RequestBody MarketplaceListing listing){

        return marketplaceService.listCredits(listing);

    }

    // =========================
    // GET ALL LISTINGS
    // =========================
    @GetMapping
    public List<MarketplaceListing> getListings(){

        return marketplaceService.getListings();

    }

    // =========================
    // BUY CREDITS
    // =========================
    @PostMapping("/buy/{id}")
    public MarketplaceListing buyCredits(@PathVariable String id){

        return marketplaceService.buyCredits(id);

    }
}