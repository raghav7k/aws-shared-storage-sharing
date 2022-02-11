package com.aws.sharedstorage.Controller;

import com.aws.sharedstorage.Service.SharedStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.services.sts.StsClient;

@RequestMapping("api/v1")

@RestController
public class SharedStorageController {
    SharedStorageService sharedStorageService;

    String RoleARN = "arn:aws:iam::844102931058:role/test_AssumeRole_VCM";

    @Autowired
    public SharedStorageController(SharedStorageService sharedStorageService) {
        this.sharedStorageService = sharedStorageService;
    }

    @PostMapping("share")
    public Object createShare(@RequestBody String requestBody) {
        return sharedStorageService.createShare(requestBody);
    }
    @GetMapping()
    public String getBuckets() {
         return sharedStorageService.getBuckets(RoleARN);
    }
}
