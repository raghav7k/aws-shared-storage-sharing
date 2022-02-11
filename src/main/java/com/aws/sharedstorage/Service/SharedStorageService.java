package com.aws.sharedstorage.Service;

import com.aws.sharedstorage.Repository.AWSRepository;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

@Service
public class SharedStorageService {

    AWSRepository awsRepository;

    @Autowired
    public SharedStorageService(AWSRepository awsRepository) {
        this.awsRepository = awsRepository;
    }


    public String getBuckets(String roleArn) {

        S3Client s3Client = S3Client.create();
        ListBucketsResponse response = s3Client.listBuckets();
        return response.toString();
        //AssumeRoleResponse assumeRoleResponse = awsRepository.assumeRole(roleArn);
        //return assumeRoleResponse.toString();
    }

    public JSONObject createShare(String body) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(body);


        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
