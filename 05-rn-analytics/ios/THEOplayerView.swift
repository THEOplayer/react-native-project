import Foundation
import UIKit
import THEOplayerSDK

@objc(THEOplayerView)
class THEOplayerView: UIView {

  var player: THEOplayer
  var onSeek: RCTBubblingEventBlock?
  var onPlay: RCTBubblingEventBlock?
  var onPause: RCTBubblingEventBlock?

  private var listeners: [String: EventListener] = [:]

  init() {
    if let appDelegate = UIApplication.shared.delegate as? AppDelegate, !appDelegate.castContextSet {
      //THEOplayerCastHelper.setGCKCastContextSharedInstanceWithDefaultCastOptions()
      appDelegate.castContextSet = true
    }

    /*
     Example conviva usage, add account code, gateway url & uncomment analytics config declaration(add or change to conviva configuration)
    */
    let convivaMetadata = ConvivaContentMetadata(
       assetName: "THEOPlayer demo",
       live: false
    )
    let conviva = ConvivaConfiguration(
       customerKey: "<Your conviva account code>",
       heartbeatInterval: 5,
       gatewayURL: "<Your conviva gateway url>",
       contentMetadata: convivaMetadata
    )

    /*
      Example youbora usage, add account code & uncomment analytics config declaration
     */
    let youbora = YouboraOptions(accountCode: "<Your youbora account code>")
    youbora.put(key: "username", value: "THEO user")
    youbora.put(key: "enableAnalytics", value: "true")
    youbora.put(key: "content.title", value: "Demo")

    let scripthPaths = [Bundle.main.path(forResource: "theoplayer", ofType: "js")].compactMap { $0 }
    let stylePaths = [Bundle.main.path(forResource: "theoplayer", ofType: "css")].compactMap { $0 }
    let playerConfig = THEOplayerConfiguration(
        chromeless: false,
        pip: nil,
        license: "your_license_string",
        cssPaths: stylePaths,
        jsPaths: scripthPaths,
        googleIMA: false,
        analytics: [youbora,conviva]
      )

    player = THEOplayer(configuration: playerConfig)
    /*
       Evaluate main script function declarated in theoplayer.js(custom js)
       You can init pure js code without file by evaluateJavaScript.
    */
    player.evaluateJavaScript("init({player: player})")

    //register player on event emitter
    EventEmitter.sharedInstance.registerPlayer(player: player)

    super.init(frame: .zero)
    player.addAsSubview(of: self)

    let seekListener = player.addEventListener(type: PlayerEventTypes.SEEKED) { [unowned self] event in
      print("Received \(event.type) event at \(event.currentTime)")
      guard self.onSeek != nil else {
        return
      }

      self.onSeek!(["currentTime": event.currentTime])
    }
    listeners[PlayerEventTypes.SEEKED.name] = seekListener

    let playListener = player.addEventListener(type: PlayerEventTypes.PLAY) { [unowned self] event in
      print("Received \(event.type) event at \(event.currentTime)")
      guard self.onPlay != nil else {
        return
      }

      self.onPlay!([:])
    }
    listeners[PlayerEventTypes.PLAY.name] = playListener

    let pauseListener = player.addEventListener(type: PlayerEventTypes.PAUSE) { [unowned self] event in
      print("Received \(event.type) event at \(event.currentTime)")
      guard self.onPause != nil else {
        return
      }

      self.onPause!([:])
    }
    listeners[PlayerEventTypes.PAUSE.name] = pauseListener
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  @objc(setSource:) func setSource(source: SourceDescription) {
    player.source = source
  }

  @objc(setAutoplay:) func setAutoplay(autoplay: Bool) {
    player.autoplay = autoplay
  }

  @objc(setFullscreenOrientationCoupling:) func setFullscreenOrientationCoupling(fullscreenOrientationCoupling: Bool) {
    player.fullscreenOrientationCoupling = fullscreenOrientationCoupling
  }

  @objc(setOnSeek:) func setOnSeek(seek: @escaping RCTBubblingEventBlock) {
    onSeek = seek
  }

  @objc(setOnPlay:) func setOnPlay(play: @escaping RCTBubblingEventBlock) {
    onPlay = play
  }

  @objc(setOnPause:) func setOnPause(pause: @escaping RCTBubblingEventBlock) {
    onPause = pause
  }

  override func layoutSubviews() {
    super.layoutSubviews()
    player.frame = frame
    player.autoresizingMask = [.flexibleBottomMargin, .flexibleHeight, .flexibleLeftMargin, .flexibleRightMargin, .flexibleTopMargin, .flexibleWidth]
  }

  deinit {
    for (eventName, listener) in listeners {
      switch eventName {
      case "play":
        player.removeEventListener(type: PlayerEventTypes.PLAY, listener: listener)

      case "pause":
        player.removeEventListener(type: PlayerEventTypes.PAUSE, listener: listener)

      case "seeked":
        player.removeEventListener(type: PlayerEventTypes.SEEKED, listener: listener)

      default:
        break
      }
    }
  }
}
