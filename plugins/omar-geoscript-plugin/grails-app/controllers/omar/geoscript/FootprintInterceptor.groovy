package omar.geoscript

import omar.core.BindUtil


class FootprintInterceptor
{

  public FootprintInterceptor()
  {
    match( controller: 'footprints' )
  }

  boolean before()
  {
    BindUtil.fixParamNames( GetFootprintsRequest, params )

    true
  }

  boolean after() { true }
  
  @Override
  void afterView()
  {
    // no-op
  }
}
