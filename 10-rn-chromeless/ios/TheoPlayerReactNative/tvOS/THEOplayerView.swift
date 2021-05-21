import Foundation
import UIKit
import THEOplayerSDK

@objc(THEOplayerView)
class THEOplayerView: BaseTHEOplayerView {

  init() {
    let playerConfig = THEOplayerConfiguration(
      chromeless: true,
      pip: nil,
      license: "your_license_string"
    )
    let player = THEOplayer(configuration: playerConfig)
    super.init(player: player)
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}
