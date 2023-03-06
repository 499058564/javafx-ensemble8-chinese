/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ensemble;

import static ensemble.PlatformFeatures.WEB_SUPPORTED;
import ensemble.samplepage.SourcePage;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import ensemble.generated.Samples;
import ensemble.samplepage.SamplePage;
import org.apache.commons.lang3.StringUtils;
import sun.net.util.URLUtil;

/**
 * Sample page navigation with history.
 *
 * Also knows how to create Ensemble pages.
 */
public class PageBrowser extends Region {
    public static final String HOME_URL = "home";
    private static final String WEB_URL_PREFIX_HTTP = "http://";
    private static final String WEB_URL_PREFIX_HTTPS = "https://";
    private static final String SAMPLE_URL_PREFIX = "sample://";
    private static final String SAMPLE_URL_PREFIX_SRC = "sample-src://";

    private HomePage homePage;
    private Page currentPage;
    private SamplePage samplePage;
    private SourcePage sourcePage;
    private String currentPageUrl;
    private DocsPage docsPage;
    private LinkedList<String> pastHistory = new LinkedList<>();
    private LinkedList<String> futureHistory = new LinkedList<>();
    private BooleanProperty forwardPossible = new SimpleBooleanProperty(false);
    public ReadOnlyBooleanProperty forwardPossibleProperty() { return forwardPossible; }
    public boolean isForwardPossible() { return forwardPossible.get(); }
    private BooleanProperty backPossible = new SimpleBooleanProperty(false);
    public ReadOnlyBooleanProperty backPossibleProperty() { return backPossible; }
    public boolean isBackPossible() { return backPossible.get(); }
    private BooleanProperty atHome = new SimpleBooleanProperty(false);
    public ReadOnlyBooleanProperty atHomeProperty() { return atHome; }
    public boolean isAtHome() { return atHome.get(); }
    private StringProperty currentPageTitle = new SimpleStringProperty(null);
    public ReadOnlyStringProperty currentPageTitleProperty() { return currentPageTitle; };
    public String getCurrentPageTitle() { return currentPageTitle.get(); }

    public void forward() {
        String newUrl = futureHistory.pop();
        if (newUrl != null) {
            pastHistory.push(getCurrentPageUrl());
            goToPage(newUrl, null, false);
        }
    }

    public void backward() {
        String newUrl = pastHistory.pop();
        if (newUrl != null) {
            futureHistory.push(getCurrentPageUrl());
            goToPage(newUrl, null, false);
        }
    }

    public void goToSample(SampleInfo sample) {
        goToPage("sample://"+sample.ensemblePath, sample, true);
    }

    public void goToPage(String url) {
        goToPage(url, null, true);
    }

    public void goHome() {
        goToPage(HOME_URL, null, true);
    }

    /**
     * This is called when a inner url has changed inside of a page and we want
     * to update the history.
     *
     * @param newUrl The new url that the currentPage node is displaying
     */
    public void externalPageChange(String newUrl) {
        if (currentPageUrl != null) {
            pastHistory.push(getCurrentPageUrl());
        }
        futureHistory.clear();
        currentPageUrl = newUrl;
    }

    private void goToPage(String url, SampleInfo sample, boolean updateHistory) {
        Page nextPage = null;
        if (url.equals(HOME_URL)) {
            nextPage = getHomePage();
        }
        if(isWebUrl(url)){
            nextPage = updateDocsPage(url);
        }
        if(url.startsWith(SAMPLE_URL_PREFIX)){
            nextPage = urlForSamplePage(url);
        }
        if(url.startsWith(SAMPLE_URL_PREFIX_SRC)){
            nextPage = urlForSampleSrcPage(url);
        }
        if(null == nextPage && null != sample){
            nextPage = updateSamplePage(sample, url);
        }
        if(null == nextPage){
            System.err.println("Unknown ensemble page url [" + url + "]");
            return;
        }
        if (updateHistory) {
            updateHistory();
        }
        removeOldPageAndUpdateCurrentPage(url , nextPage);
        goToPageAfterUpdateProperties();
    }
    private boolean isWebUrl(String url){
        boolean webNotSupported = !WEB_SUPPORTED;
        if(webNotSupported){
            System.err.println("Web pages are not supported and links to them should be disabled!");
            return false;
        }
        boolean isWebUrl = url.startsWith(WEB_URL_PREFIX_HTTP) || url.startsWith(WEB_URL_PREFIX_HTTPS);
        return  isWebUrl;
    }

    private Page urlForSamplePage(String url){
        final SampleInfo sampleInfo = getSampleInfoByUrl(url , SAMPLE_URL_PREFIX);
        if(null == sampleInfo){
            throw new UnsupportedOperationException("Unknown sample url ["+url+"]");
        }
        return updateSamplePage(sampleInfo, url);
    }

    private SourcePage urlForSampleSrcPage(String url){
        final SampleInfo sampleInfo = getSampleInfoByUrl(url , SAMPLE_URL_PREFIX_SRC);
        if(null == sampleInfo){
            throw new UnsupportedOperationException("Unknown sample url ["+url+"]");
        }
        return updateSourcePage(sampleInfo);
    }

    private SampleInfo getSampleInfoByUrl(String url , String sanpleUrlPrefix){
        String samplePath = url.substring(sanpleUrlPrefix.length());
        if (samplePath.contains("?")) {
            samplePath = samplePath.substring(0, samplePath.indexOf('?') - 1);
        }
        return Samples.ROOT.sampleForPath(samplePath);
    }

    private void updateHistory(){
        if (currentPageUrl != null) {
            pastHistory.push(getCurrentPageUrl());
        }
        futureHistory.clear();
    }

    private void removeOldPageAndUpdateCurrentPage(String url , Page nextPage){
        currentPageUrl = url;
        if (currentPage != null) {
            getChildren().remove((Node) currentPage);
        }
        currentPage = nextPage;
        getChildren().add(currentPage.getNode());
    }

    private void goToPageAfterUpdateProperties(){
        atHome.set(currentPageUrl.equals(HOME_URL));
        forwardPossible.set(!futureHistory.isEmpty());
        backPossible.set(!pastHistory.isEmpty());
        currentPageTitle.bind(currentPage.titleProperty());
    }

    @Override protected void layoutChildren() {
        if (currentPage != null) {
            currentPage.getNode().resize(getWidth(), getHeight());
        }
    }

    public String getCurrentPageUrl() {
        return currentPageUrl;
    }

    private SamplePage updateSamplePage(SampleInfo sample, String url) {
        if (samplePage == null) {
            samplePage = new SamplePage(sample, url, this);
        } else {
            samplePage.update(sample, url);
        }
        return samplePage;
    }

    private SourcePage updateSourcePage(SampleInfo sample) {
        if (sourcePage == null) {
            sourcePage = new SourcePage();
        }
        sourcePage.setSampleInfo(sample);
        return sourcePage;
    }

    private Page getHomePage() {
        if (homePage == null) {
            homePage = new HomePage(this);
        }
        return homePage;
    }

    private DocsPage updateDocsPage(String url) {
        if (docsPage == null) {
            docsPage = new DocsPage(this);
        }
        docsPage.goToUrl(url);
        return docsPage;
    }
}
