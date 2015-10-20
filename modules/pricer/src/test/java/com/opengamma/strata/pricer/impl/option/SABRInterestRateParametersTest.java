/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.impl.option;

import static com.opengamma.strata.collect.TestHelper.coverBeanEquals;
import static com.opengamma.strata.collect.TestHelper.coverImmutableBean;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.strata.basics.PutCall;
import com.opengamma.strata.collect.array.DoubleArray;
import com.opengamma.strata.collect.tuple.DoublesPair;
import com.opengamma.strata.market.surface.ConstantNodalSurface;
import com.opengamma.strata.market.surface.DefaultSurfaceMetadata;
import com.opengamma.strata.market.surface.InterpolatedNodalSurface;
import com.opengamma.strata.market.surface.NodalSurface;
import com.opengamma.strata.market.surface.SurfaceMetadata;
import com.opengamma.strata.math.impl.interpolation.GridInterpolator2D;
import com.opengamma.strata.math.impl.interpolation.LinearInterpolator1D;
import com.opengamma.strata.pricer.impl.volatility.smile.function.SABRFormulaData;
import com.opengamma.strata.pricer.impl.volatility.smile.function.SABRHaganVolatilityFunctionProvider;
import com.opengamma.strata.pricer.impl.volatility.smile.function.VolatilityFunctionProvider;

/**
 * Test {@link SABRInterestRateParameters}.
 */
@Test
public class SABRInterestRateParametersTest {

  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final GridInterpolator2D GRID = new GridInterpolator2D(LINEAR, LINEAR);
  private static final SurfaceMetadata METADATA = DefaultSurfaceMetadata.of("surface");
  private static final InterpolatedNodalSurface ALPHA_SURFACE = InterpolatedNodalSurface.of(METADATA,
      DoubleArray.of(0.0, 10, 0.0, 10), DoubleArray.of(0, 0, 10, 10), DoubleArray.of(0.2, 0.2, 0.2, 0.2), GRID);
  private static final InterpolatedNodalSurface BETA_SURFACE = InterpolatedNodalSurface.of(METADATA,
      DoubleArray.of(0.0, 10, 0.0, 10), DoubleArray.of(0, 0, 10, 10), DoubleArray.of(1, 1, 1, 1), GRID);
  private static final InterpolatedNodalSurface RHO_SURFACE = InterpolatedNodalSurface.of(METADATA,
      DoubleArray.of(0.0, 10, 0.0, 10), DoubleArray.of(0, 0, 10, 10), DoubleArray.of(-0.5, -0.5, -0.5, -0.5), GRID);
  private static final InterpolatedNodalSurface NU_SURFACE = InterpolatedNodalSurface.of(METADATA,
      DoubleArray.of(0.0, 10, 0.0, 10), DoubleArray.of(0, 0, 10, 10), DoubleArray.of(0.5, 0.5, 0.5, 0.5), GRID);
  private static final SABRHaganVolatilityFunctionProvider FUNCTION = SABRHaganVolatilityFunctionProvider.DEFAULT;
  private static final SABRInterestRateParameters PARAMETERS =
      SABRInterestRateParameters.of(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlpha() {
    NodalSurface surface = null;
    SABRInterestRateParameters.of(surface, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBeta() {
    NodalSurface surface = null;
    SABRInterestRateParameters.of(ALPHA_SURFACE, surface, RHO_SURFACE, NU_SURFACE, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRho() {
    NodalSurface surface = null;
    SABRInterestRateParameters.of(ALPHA_SURFACE, BETA_SURFACE, surface, NU_SURFACE, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNu() {
    NodalSurface surface = null;
    SABRInterestRateParameters.of(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, surface, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShift() {
    NodalSurface surface = null;
    SABRInterestRateParameters.of(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION, surface);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    SABRInterestRateParameters.of(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE,
        (VolatilityFunctionProvider<SABRFormulaData>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    PARAMETERS.getVolatility(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongData() {
    PARAMETERS.getVolatility(new double[] {1, 2, 3 });
  }

  @Test
  public void hashEqualGetter() {
    assertEquals(PARAMETERS.getAlphaSurface(), ALPHA_SURFACE);
    assertEquals(PARAMETERS.getBetaSurface(), BETA_SURFACE);
    assertEquals(PARAMETERS.getRhoSurface(), RHO_SURFACE);
    assertEquals(PARAMETERS.getNuSurface(), NU_SURFACE);
    assertEquals(PARAMETERS.getSabrFunctionProvider(), FUNCTION);
    assertEquals(PARAMETERS.getShiftSurface(), ConstantNodalSurface.of("zero shift", 0d));
    double expiry = 2.0;
    double tenor = 3.0;
    DoublesPair sample = DoublesPair.of(expiry, tenor);
    assertEquals(PARAMETERS.getAlpha(sample), ALPHA_SURFACE.zValue(sample));
    assertEquals(PARAMETERS.getBeta(sample), BETA_SURFACE.zValue(sample));
    assertEquals(PARAMETERS.getRho(sample), RHO_SURFACE.zValue(sample));
    assertEquals(PARAMETERS.getNu(sample), NU_SURFACE.zValue(sample));
    double strike = 1.1;
    double forward = 1.05;
    EuropeanVanillaOption option = EuropeanVanillaOption.of(strike, expiry, PutCall.CALL);
    SABRFormulaData data = SABRFormulaData.of(
        ALPHA_SURFACE.zValue(sample), BETA_SURFACE.zValue(sample), RHO_SURFACE.zValue(sample), NU_SURFACE.zValue(sample));
    assertEquals(PARAMETERS.getVolatility(expiry, tenor, strike, forward), FUNCTION.getVolatility(option, forward, data));
    assertEquals(PARAMETERS.getVolatility(new double[] {expiry, tenor, strike, forward }),
        FUNCTION.getVolatility(option, forward, data));
    double[] modelAdjCmp = PARAMETERS.getVolatilityModelAdjoint(expiry, tenor, strike, forward);
    double[] modelAdjExp = FUNCTION.getVolatilityModelAdjoint(option, forward, data);
    for (int i = 0; i < 4; ++i) {
      assertEquals(modelAdjCmp[i], modelAdjExp[i]);
    }
    double[] adjCmp = PARAMETERS.getVolatilityAdjoint(expiry, tenor, strike, forward);
    double[] adjExp = FUNCTION.getVolatilityAdjoint(option, forward, data);
    for (int i = 0; i < 7; ++i) {
      assertEquals(adjCmp[i], adjExp[i]);
    }
    SABRInterestRateParameters other =
        SABRInterestRateParameters.of(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);
    assertEquals(PARAMETERS, other);
    assertEquals(PARAMETERS.hashCode(), other.hashCode());
    other = SABRInterestRateParameters.of(BETA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);
    assertFalse(other.equals(PARAMETERS));
    other = SABRInterestRateParameters.of(ALPHA_SURFACE, ALPHA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);
    assertFalse(other.equals(PARAMETERS));
    other = SABRInterestRateParameters.of(ALPHA_SURFACE, BETA_SURFACE, ALPHA_SURFACE, NU_SURFACE, FUNCTION);
    assertFalse(other.equals(PARAMETERS));
    other = SABRInterestRateParameters.of(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, ALPHA_SURFACE, FUNCTION);
    assertFalse(other.equals(PARAMETERS));
    assertFalse(other.equals(PARAMETERS));
  }

  @Test
  public void negativeRates() {
    double shift = 0.05;
    NodalSurface surface = ConstantNodalSurface.of("shfit", shift);
    SABRInterestRateParameters params =
        SABRInterestRateParameters.of(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION, surface);
    double expiry = 2.0;
    double tenor = 3.0;
    DoublesPair sample = DoublesPair.of(expiry, tenor);
    assertEquals(params.getAlpha(sample), ALPHA_SURFACE.zValue(sample));
    assertEquals(params.getBeta(sample), BETA_SURFACE.zValue(sample));
    assertEquals(params.getRho(sample), RHO_SURFACE.zValue(sample));
    assertEquals(params.getNu(sample), NU_SURFACE.zValue(sample));
    double strike = -0.02;
    double forward = 0.015;
    EuropeanVanillaOption option = EuropeanVanillaOption.of(strike + shift, expiry, PutCall.CALL);
    SABRFormulaData data = SABRFormulaData.of(
        ALPHA_SURFACE.zValue(sample), BETA_SURFACE.zValue(sample), RHO_SURFACE.zValue(sample), NU_SURFACE.zValue(sample));
    assertEquals(params.getVolatility(expiry, tenor, strike, forward),
        FUNCTION.getVolatility(option, forward + shift, data));
    assertEquals(params.getVolatility(new double[] {expiry, tenor, strike, forward }),
        FUNCTION.getVolatility(option, forward + shift, data));
    double[] adjCmp = params.getVolatilityModelAdjoint(expiry, tenor, strike, forward);
    double[] adjExp = FUNCTION.getVolatilityModelAdjoint(option, forward + shift, data);
    for (int i = 0; i < 4; ++i) {
      assertEquals(adjCmp[i], adjExp[i]);
    }
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    coverImmutableBean(PARAMETERS);
    InterpolatedNodalSurface surface = InterpolatedNodalSurface.of(METADATA,
        DoubleArray.of(0.0, 5, 0.0, 5), DoubleArray.of(0, 0, 8, 8), DoubleArray.of(0.2, 0.3, 0.2, 0.3), GRID);
    SABRInterestRateParameters other = SABRInterestRateParameters.of(surface, surface, surface, surface, FUNCTION, surface);
    coverBeanEquals(PARAMETERS, other);
  }

}
