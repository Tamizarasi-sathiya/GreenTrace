package com.greentrace.greentrace.service;

import com.greentrace.greentrace.model.Credit;
import com.greentrace.greentrace.repository.CreditRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreditService {

    @Autowired
    private CreditRepository creditRepository;

    public Credit issueCredits(String projectId,int credits,String owner){

        Credit credit = new Credit();

        credit.setProjectId(projectId);
        credit.setAmount(credits);
        credit.setOwner(owner);

        return creditRepository.save(credit);

    }

}