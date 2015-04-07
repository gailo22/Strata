/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.engine.marketdata.mapping;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.marketdata.id.MarketDataVendor;

/**
 * Builder for {@link MarketDataMappings} that knows about the standard mappings (e.g. curve to curve group)
 * and also allows arbitrary mappings to be added.
 */
public class MarketDataMappingsBuilder {

  /** Market data vendor system that is the source for observable market data, for example Bloomberg or Reuters. */
  private MarketDataVendor marketDataVendor = MarketDataVendor.NONE;

  /**
   * Mappings that translate data requests from calculators into requests that can be used to look
   * up the data in the global set of market data.
   */
  private List<MarketDataMapping<?, ?>> mappings = new ArrayList<>();

  MarketDataMappingsBuilder() {
  }

  /**
   * Adds a mapping that sets the curve group used to look up curves.
   *
   * @param curveGroupName  the curve group used as the source for curves
   * @return this builder
   */
  public MarketDataMappingsBuilder curveGroup(String curveGroupName) {
    ArgChecker.notEmpty(curveGroupName, "curveGroupName");
    mappings.add(DiscountingCurveMapping.of(curveGroupName));
    mappings.add(IndexCurveMapping.of(curveGroupName));
    return this;
  }

  /**
   * Adds a mapping that sets the source of observable market data
   *
   * @param vendor  the vendor that is the source of observable market data
   * @return this builder
   */
  public MarketDataMappingsBuilder marketDataVendor(MarketDataVendor vendor) {
    marketDataVendor = ArgChecker.notNull(vendor, "vendor");
    return this;
  }

  /**
   * Adds a an arbitrary mapping to the builder.
   *
   * @param mapping  a mapping that defines how market data requested by a calculation should be looked up
   *   in the global set of market data
   * @return this builder
   */
  public MarketDataMappingsBuilder mapping(MarketDataMapping<?, ?> mapping) {
    ArgChecker.notNull(mapping, "mapping");
    mappings.add(mapping);
    return this;
  }

  /**
   * Returns a set of market data mappings built from the data in this builder.
   *
   * @return a set of market data mappings built from the data in this builder
   */
  public MarketDataMappings build() {
    return MarketDataMappings.of(marketDataVendor, mappings);
  }
}