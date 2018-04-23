package omar.geoscript

class GeorssController {
  def georssService

  def index() {
    render georssService.renderFeed(params)
  }
}
