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
package ensemble.samplepage;

import ensemble.Page;
import ensemble.SampleInfo;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.TabPane;

/**
 * Page showing tabs with all the source code and resources for a sample
 */
public class SourcePage extends TabPane implements Page {
    private final ObjectProperty<SampleInfo> sampleInfoProperty = new SimpleObjectProperty<>();
    private final StringProperty titleProperty = new SimpleStringProperty();

    public SourcePage() {
        getStyleClass().add("source-page");
        titleProperty.bind(new StringBinding() {
            { bind(sampleInfoProperty); }
            @Override protected String computeValue() {
                SampleInfo sample = sampleInfoProperty.get();
                if (sample != null) {
                    return sample.name+" :: Source";
                } else {
                    return null;
                }
            }
        });
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    }

    public void setSampleInfo(SampleInfo sampleInfo) {
        sampleInfoProperty.set(sampleInfo);
        getTabs().clear();
        //调整查看源文件逻辑:只显示主要源文件
        getTabs().add(new SourceTab(new SampleInfo.URL() {
            @Override
            public String getURL() {
                return sampleInfo.mainFileUrl;
            }

            @Override
            public String getName() {
                String url = getURL();
                return url.substring(url.lastIndexOf('/') + 1);
            }
        }));
        /*for (SampleInfo.URL sourceURL : sampleInfo.getSources()) {
            getTabs().add(new SourceTab(sourceURL));
        }*/
    }

    @Override public ReadOnlyStringProperty titleProperty() {
        return titleProperty;
    }

    @Override public String getTitle() {
        return titleProperty.get();
    }

    @Override public String getUrl() {
        return "sample-src://" + sampleInfoProperty.get().ensemblePath;
    }

    @Override public Node getNode() {
        return this;
    }
}
