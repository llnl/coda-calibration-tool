/*
* Copyright (c) 2019, Lawrence Livermore National Security, LLC. Produced at the Lawrence Livermore National Laboratory
* CODE-743439.
* All rights reserved.
* This file is part of CCT. For details, see https://github.com/LLNL/coda-calibration-tool.
*
* Licensed under the Apache License, Version 2.0 (the “Licensee”); you may not use this file except in compliance with the License.  You may obtain a copy of the License at:
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and limitations under the license.
*
* This work was performed under the auspices of the U.S. Department of Energy
* by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
*/
package gov.llnl.gnem.apps.coda.calibration.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;


import gov.llnl.gnem.apps.coda.calibration.model.domain.SiteFrequencyBandParameters;
import gov.llnl.gnem.apps.coda.calibration.model.domain.SpectraMeasurementMetadata;
import gov.llnl.gnem.apps.coda.calibration.model.domain.SpectraMeasurementMetadataImpl;
import gov.llnl.gnem.apps.coda.calibration.model.domain.WaveformMetadataImpl;
import gov.llnl.gnem.apps.coda.calibration.model.domain.mixins.SharedFrequencyBandParametersJsonMixin;
import gov.llnl.gnem.apps.coda.calibration.model.domain.mixins.SiteFrequencyBandParametersJsonMixin;
import gov.llnl.gnem.apps.coda.common.model.domain.SharedFrequencyBandParameters;
import gov.llnl.gnem.apps.coda.common.model.domain.WaveformMetadata;
import tools.jackson.core.Version;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleAbstractTypeResolver;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class WebfluxConfig {

    private final JsonMapper objectMapper;

    @Autowired
    public WebfluxConfig(JsonMapper.Builder builder) {
        builder.addMixIn(SharedFrequencyBandParameters.class, SharedFrequencyBandParametersJsonMixin.class);
        builder.addMixIn(SiteFrequencyBandParameters.class, SiteFrequencyBandParametersJsonMixin.class);

        SimpleModule module = new SimpleModule("SpectraMeasurementMapper", Version.unknownVersion());
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(SpectraMeasurementMetadata.class, SpectraMeasurementMetadataImpl.class);
        resolver.addMapping(WaveformMetadata.class, WaveformMetadataImpl.class);
        module.setAbstractTypes(resolver);
        builder.addModule(module);
        
        this.objectMapper = builder.build();
    }

    @Bean
    public ExchangeStrategies configureJacksonExchangeStrategies() {
        return ExchangeStrategies.builder().codecs(clientCodecConfigurer -> {
            JacksonJsonDecoder decoder = new JacksonJsonDecoder(objectMapper);
            decoder.setMaxInMemorySize(-1);
            clientCodecConfigurer.customCodecs().registerWithDefaultConfig(decoder);
            clientCodecConfigurer.customCodecs().registerWithDefaultConfig(new JacksonJsonEncoder(objectMapper));
            //Unlimited
            clientCodecConfigurer.defaultCodecs().maxInMemorySize(-1);
        }).build();
    }
}
