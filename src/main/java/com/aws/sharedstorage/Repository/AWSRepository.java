package com.aws.sharedstorage.Repository;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.ArrayList;

@Repository
public class AWSRepository {

    public BasicSessionCredentials assumeRole(String roleArn) {
        String clientRegion = "us-east-1";
        // String roleARN = "arn:aws:iam::844102931058:role/test_AssumeRole_VCM";
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(clientRegion)
                .build();

        // Obtain credentials for the IAM role. Note that you cannot assume the role of an AWS root account;
        // Amazon S3 will deny access. You must use credentials for an IAM user or an IAM role.
        com.amazonaws.services.securitytoken.model.AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                .withRoleArn(roleArn)
                .withRoleSessionName("session");
        AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
        Credentials sessionCredentials = roleResponse.getCredentials();

        // Create a BasicSessionCredentials object that contains the credentials you just retrieved.
        BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
                sessionCredentials.getAccessKeyId(),
                sessionCredentials.getSecretAccessKey(),
                sessionCredentials.getSessionToken());

        return awsCredentials;
    }
    public ArrayList<String> getBucketsWithAssume(BasicSessionCredentials awsCredentials) {
        String clientRegion = "us-east-1";

        // Provide temporary security credentials so that the Amazon S3 client
        // can send authenticated requests to Amazon S3. You create the client
        // using the sessionCredentials object.
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(clientRegion)
                .build();

        // Verify that assuming the role worked and the permissions are set correctly
        // by getting a set of object keys from the bucket.

        System.out.println(s3.listBuckets());
        ArrayList<String> buckets = new ArrayList<>();
        s3.listBuckets().stream().forEach(x -> buckets.add(x.getName()));

        return buckets;

    }
    public ListBucketsResponse listBuckets() {
        S3Client s3Client = S3Client.create();
        return s3Client.listBuckets();
    }

    public int getBucketsObjects(String bucket) {
        BasicSessionCredentials awsCredentials = this.assumeRole("arn:aws:iam::844102931058:role/test_AssumeRole_VCM");
        try {
            AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials) )
                    .withRegion("us-west-2")
                    .build();

            return s3.listObjects(bucket).getObjectSummaries().size();
        } catch(Exception e) {
            return -1;
        }

    }

}
