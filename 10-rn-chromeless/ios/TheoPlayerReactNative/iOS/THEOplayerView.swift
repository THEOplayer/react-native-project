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
     If you want to use Google Ima set googleIMA in theoplayer config(set true googleIMA in the line below) and add 'integration: "google-ima"' in js ads declaration.
     You can declarate in THEOplayer configuration builder default js and css paths by using cssPaths() and jsPaths()
     
     let scripthPaths = [Bundle.main.path(forResource: "theoplayer", ofType: "js")].compactMap { $0 }
     let stylePaths = [Bundle.main.path(forResource: "theoplayer", ofType: "css")].compactMap { $0 }
     */
    
    let playerConfig: THEOplayerConfiguration
    
    playerConfig = THEOplayerConfiguration(
      chromeless: true,
      googleIMA: false,
      pip: nil
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
