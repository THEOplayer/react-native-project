import Foundation
import UIKit
import THEOplayerSDK

@objc(THEOplayerView)
class THEOplayerView: BaseTHEOplayerView {

  init() {
    if let appDelegate = UIApplication.shared.delegate as? AppDelegate, !appDelegate.castContextSet {
      THEOplayerCastHelper.setGCKCastContextSharedInstanceWithDefaultCastOptions()
      appDelegate.castContextSet = true
    }

    /*
     You can declarate in THEOplayer configuration builder default js and css paths by using cssPaths() and jsPaths()

     let scripthPaths = [Bundle.main.path(forResource: "theoplayer", ofType: "js")].compactMap { $0 }
     let stylePaths = [Bundle.main.path(forResource: "theoplayer", ofType: "css")].compactMap { $0 }
     */

    let playerConfig: THEOplayerConfiguration

    playerConfig = THEOplayerConfiguration(
      chromeless: true,
      pip: nil,
      license: "your_license_string"
    )

    let player = THEOplayer(configuration: playerConfig)
    player.evaluateJavaScript("init({player: player})")

    super.init(player: player)
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  @objc(setFullscreenOrientationCoupling:) override func setFullscreenOrientationCoupling(fullscreenOrientationCoupling: Bool) {
    theoplayer.fullscreenOrientationCoupling = false
  }

}
