package com.example.comp90018.ui;

import android.os.Build;
//import android.support.annotation.MainThread;
//import android.support.annotation.RequiresApi;

import androidx.annotation.RequiresApi;

import com.microsoft.azure.cognitiveservices.faceapi.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionClientImpl;
import com.microsoft.azure.management.Azure;

import java.io.*;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TestTest {
    // <snippet_creds>
    static String subscriptionKey = "2fba4493b842452a8cda2fb26e2f6b1d";
    static String endpoint = "https://computer-vision-zisen-demo.cognitiveservices.azure.com/";
    // </snippet_creds>
    public static void main(String[] args) {
        System.out.println("\nAzure Cognitive Services Computer Vision - Java Quickstart Sample");
        // Create an authenticated Computer Vision client.
        ComputerVisionClient compVisClient = Authenticate(subscriptionKey, endpoint);

        // Read from local file
        ReadFromUrl(compVisClient);
    }

    public static ComputerVisionClient Authenticate(String subscriptionKey, String endpoint){
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
    }

    private static void ReadFromUrl(ComputerVisionClient client) {
        System.out.println("-----------------------------------------------");
        String remoteTextImageURL = "https://cdn-edlaf.nitrocdn.com/MrNRpIDxETuUgyzZDDndpsemlknGClCx/assets/static/optimized/rev-e5316bb/wp-content/uploads/2017/10/cosmetic-injections-gummy-smile-correction.jpg";
        System.out.println("Read with URL: " + remoteTextImageURL);
        try {
            // Cast Computer Vision to its implementation to expose the required methods
            ComputerVisionImpl vision = (ComputerVisionImpl) client.computerVision();

            // Read in remote image and response header
            ReadHeaders responseHeader = vision.readWithServiceResponseAsync(remoteTextImageURL,null)
                    .toBlocking()
                    .single()
                    .headers();

            // Extract the operation Id from the operationLocation header
            String operationLocation = responseHeader.operationLocation();
            System.out.println("Operation Location:" + operationLocation);

            getAndPrintReadResult(vision, operationLocation);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // <snippet_read_setup>
    /**
     * OCR with READ : Performs a Read Operation on a local image
     * @param client instantiated vision client
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void ReadFromFile(ComputerVisionClient client) {
        System.out.println("-----------------------------------------------");

        String localFilePath = "src\\main\\resources\\myImage.png";
        System.out.println("Read with local file: " + localFilePath);
        // </snippet_read_setup>
        // <snippet_read_call>

        try {
            File rawImage = new File(localFilePath);
            byte[] localImageBytes = Files.readAllBytes(rawImage.toPath());

            // Cast Computer Vision to its implementation to expose the required methods
            ComputerVisionImpl vision = (ComputerVisionImpl) client.computerVision();

            // Read in remote image and response header
            ReadInStreamHeaders responseHeader =
                    vision.readInStreamWithServiceResponseAsync(localImageBytes, null)
                            .toBlocking()
                            .single()
                            .headers();
            // </snippet_read_call>
            // <snippet_read_response>
            // Extract the operationLocation from the response header
            String operationLocation = responseHeader.operationLocation();
            System.out.println("Operation Location:" + operationLocation);

            getAndPrintReadResult(vision, operationLocation);
            // </snippet_read_response>
            // <snippet_read_catch>

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    // </snippet_read_catch>

    // <snippet_opid_extract>
    /**
     * Extracts the OperationId from a Operation-Location returned by the POST Read operation
     * @param operationLocation
     * @return operationId
     */
    private static String extractOperationIdFromOpLocation(String operationLocation) {
        if (operationLocation != null && !operationLocation.isEmpty()) {
            String[] splits = operationLocation.split("/");

            if (splits != null && splits.length > 0) {
                return splits[splits.length - 1];
            }
        }
        throw new IllegalStateException("Something went wrong: Couldn't extract the operation id from the operation location");
    }
    // </snippet_opid_extract>

    // <snippet_read_result_helper_call>
    /**
     * Polls for Read result and prints results to console
     * @param vision Computer Vision instance
     * @return operationLocation returned in the POST Read response header
     */
    private static void getAndPrintReadResult(ComputerVision vision, String operationLocation) throws InterruptedException {
        System.out.println("Polling for Read results ...");

        // Extract OperationId from Operation Location
        String operationId = extractOperationIdFromOpLocation(operationLocation);

        boolean pollForResult = true;
        ReadOperationResult readResults = null;

        while (pollForResult) {
            // Poll for result every second
            Thread.sleep(1000);
            readResults = vision.getReadResult(UUID.fromString(operationId));

            // The results will no longer be null when the service has finished processing the request.
            if (readResults != null) {
                // Get request status
                OperationStatusCodes status = readResults.status();

                if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                    pollForResult = false;
                }
            }
        }
        // </snippet_read_result_helper_call>

        // <snippet_read_result_helper_print>
        // Print read results, page per page
        for (ReadResult pageResult : readResults.analyzeResult().readResults()) {
            System.out.println("");
            System.out.println("Printing Read results for page " + pageResult.page());
            StringBuilder builder = new StringBuilder();

            for (Line line : pageResult.lines()) {
                builder.append(line.text());
                builder.append("\n");
            }

            System.out.println(builder.toString());
        }
    }
    // </snippet_read_result_helper_print>
    // <snippet_classdef_2>
}