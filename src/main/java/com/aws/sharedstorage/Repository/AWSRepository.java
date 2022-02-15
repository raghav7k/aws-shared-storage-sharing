package com.aws.sharedstorage.Repository;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

@Repository
public class AWSRepository {
    public AssumeRoleResponse assumeRole(String roleArn) {
        StsClient stsClient = StsClient.create();
        AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                .roleArn(roleArn)
                .roleSessionName("Buckets")
                .build();

        AssumeRoleResponse assumeRoleResponse = stsClient.assumeRole(assumeRoleRequest);
        return assumeRoleResponse;
    }
}
