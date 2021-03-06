/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License") you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
@Grab('org.apache.httpcomponents:httpclient:4.5')
@Grab('org.apache.httpcomponents:httpmime:4.5')
@Grab('commons-io:commons-io:2.4')
@Grab('log4j:log4j:1.2.17')

import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.*
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.apache.http.entity.ContentType
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import static org.apache.http.HttpStatus.*

import org.apache.commons.io.IOUtils

import groovy.json.JsonOutput
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import org.apache.log4j.*
import groovy.util.logging.*

import groovy.transform.CompileStatic

/**
 * A simple example that uses HttpClient that requires user authentication using JSESSIONID.
 */
@CompileStatic
@Log4j
class HttpClient {
    DefaultHttpClient httpclient
    String accessToken


    static String SERVICE_NAME = 'Service'
    static String SERVER = "http://localhost:8080/$SERVICE_NAME"
    static String LOG_IN_URL = "$SERVER/api/login"
    static String LOG_OUT_URL = "$SERVER/api/logout"

    static String GET_URL = "$SERVER/controller/action"
    static String UPLOAD_URL = "$SERVER/controller/action"

    HttpClient(){
        log.level = Level.INFO
        log.addAppender(new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss,SSS}-%t-%x-%-5p-%-10c:%m%n"), 'test_integrated_analytics.log'));

        print "Enter username: "
        String username = System.console().readLine()
        print "Enter password: "
        char[] password = System.console().readPassword()
        logIn(username, password.toString())
    }

    void logIn(String userName, String password){
        httpclient = new DefaultHttpClient()
        
        
        try {
            Map post = executePost(LOG_IN_URL, null, null, [username: userName, password: password]) as Map
            accessToken = post.access_token

            log.info "logged in"
        } catch(Exception e){
            log.error e.getMessage()        
        }
    }

    Object executeGet(String url){
        HttpGet get = new HttpGet(url)
        executeHttpCall(get)
    }

    void getDownload(String url, String ouputFileName){
        OutputStream outputStream
        
        HttpGet get = new HttpGet(url)
        get.setHeader('Authorization', "Bearer $accessToken")
        HttpResponse response = httpclient.execute(get)

        if (response.getStatusLine() == SC_OK) {
            try{
                InputStream inputStream = new BufferedInputStream(response.getEntity().getContent())
                outputStream = new BufferedOutputStream(new FileOutputStream(new File(ouputFileName)))
                IOUtils.copy(inputStream,outputStream)
                outputStream.flush()
            } finally {
                outputStream.close()
            }
        } else{
            log.error response.getStatusLine()
        }
    }

    Object executePut(String url, Map jsonBody){
        HttpPut put = new HttpPut(url)
        executeHttpCall(put, null, null, jsonBody)
    }

    void executeDelete(String url){
        HttpDelete delete = new HttpDelete(url)
        executeHttpCall(delete)
    }

    Object executePost(String url, Map filePaths = null, Map params = null, Map jsonBody = null) {
        HttpPost post = new HttpPost(url)
        executeHttpCall(post, filePaths, params, jsonBody)
    }

    Object executeHttpCall(HttpRequestBase request, Map filePaths = null, Map params = null, Map jsonBody = null) {
        try {
            Object resonseObject = ""
            if(accessToken) {
                request.setHeader('Authorization', "Bearer $accessToken")
            }
            request.setHeader("Connection", "keep-alive")

            if(params){
                addParams(request as HttpEntityEnclosingRequestBase, params)
            }

            if(filePaths){
                addFiles(request, filePaths)
            }

            if (jsonBody){
                (request as HttpEntityEnclosingRequestBase).setEntity(new StringEntity(new JsonBuilder(jsonBody).toString()) as HttpEntity)
            }
            
            HttpResponse response = httpclient.execute(request)
            log.info response.getStatusLine()
            HttpEntity entity = response.getEntity()

            if (entity) {
                String responseText = entity.getContent().getText()
                JsonSlurper slurper = new JsonSlurper()

                try{
                    resonseObject = slurper.parseText(responseText)
                }catch(Exception ep){
                    //not json
                    log.warn "request response is text not JSON"
                    resonseObject = responseText
                }
            }

            EntityUtils.consume(response.getEntity())
            return resonseObject

        } catch (Exception e) {
            log.info e.getMessage()
            e.printStackTrace() 
        }finally {
            request.releaseConnection()
        }
    }

    void addParams(HttpEntityEnclosingRequestBase request, Map<String,String> map){
        List <BasicNameValuePair> nvps = map.collect{ String key, String value ->
            new BasicNameValuePair(key, value)
        }
        
        request.setEntity(new UrlEncodedFormEntity(nvps) as HttpEntity)
    }


    void addFiles(HttpRequestBase request, Map<String,String> filePaths){
        filePaths.each{ String name, String filePath ->
            File uploadFile = new File(filePath)
            if (!uploadFile.exists()) {
                throw new FileNotFoundException(filePath)
            }

            addFile(request, name, uploadFile)
            log.info "File path: $filePath"
        }
    }

    void addFile(HttpRequestBase request,String name, File uploadFile){

        FileBody bin = new FileBody(uploadFile)
        StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN)

        HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("bin", bin)
                .addPart("comment", comment)
                .build()

        (request as HttpEntityEnclosingRequestBase).setEntity(entity)


    }

    void logOut(){
        try{
            executePost(LOG_OUT_URL)
        } finally {
            httpclient.getConnectionManager().shutdown()
            log.info "logged out"
        }
    }

    //*************************************************************************


    static void main(String[] args) throws Exception {
        HttpClient httpClient
        try{
            httpClient  = new HttpClient()
            //println httpClient.executePost(UPLOAD_URL, ["attachment":"/home/username/some_file.txt"])
            //println httpClient.executeGet(GET_URL)
        }finally{
            httpClient?.logOut()
        }
    }
}
