package com.puresoltechnologies.maven.plugins.license.stubs;

import java.util.Collections;
import java.util.List;

import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

public class TestSettingsStub extends Settings {

    private static final long serialVersionUID = -9186683022755474627L;

    @Override
    public List<Proxy> getProxies() {
        return Collections.emptyList();
    }

}
