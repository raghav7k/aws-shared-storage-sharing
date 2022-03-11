package com.aws.sharedstorage.Controller;

import com.aws.sharedstorage.Service.SharedStorageService;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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

    @GetMapping("share")
    public Object getShares() {
        return sharedStorageService.getShares(null);
    }

    @GetMapping("share/{share-id}")
    public Object getShare(@PathVariable("share-id") String shareId) {
        return sharedStorageService.getShares(shareId);
    }

    @PatchMapping("share/{share-id}")
    public Object updateShare(@RequestBody String requestBody,
                              @PathVariable("share-id") String shareId) {
        return sharedStorageService.updateShare(shareId, requestBody);
    }

    @DeleteMapping("share/{shareId}")
    public JSONObject deleteDataset(@PathVariable("shareId") String shareId) {
        return sharedStorageService.deleteShare(shareId);
    }

    @GetMapping()
    public ArrayList<String> getBuckets() {
         return sharedStorageService.getBuckets(RoleARN);
    }

    @PostMapping("share/{shareId}/datasets")
    public JSONObject addDatasets(@RequestBody String dataset,
                              @PathVariable("shareId") String shareId) {
        return sharedStorageService.addDatasets(shareId, dataset);
    }

    @GetMapping("share/{shareId}/datasets")
    public JSONArray getDatasets(@PathVariable("shareId") String shareId) {
        return sharedStorageService.getDatasets(shareId);
    }
    @PatchMapping("share/{shareId}/datasets/{datasetId}")
    public JSONObject updateDataset(@PathVariable("shareId") String shareId,
                                @PathVariable("datasetId") String datasetId,
                                @RequestBody String requestBody) {
        return sharedStorageService.updateDataset(shareId, datasetId, requestBody);
    }

    @DeleteMapping("share/{shareId}/datasets/{datasetId}")
    public JSONObject deleteDataset(@PathVariable("shareId") String shareId,
                                    @PathVariable("datasetId") String datasetId) {
        return sharedStorageService.deleteDataset(shareId, datasetId);
    }


}
