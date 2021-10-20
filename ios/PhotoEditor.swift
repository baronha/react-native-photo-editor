//
//  PhotoEditor.swift
//  PhotoEditor
//
//  Created by Donquijote on 27/07/2021.
//

import Foundation
import UIKit
import Photos
import ZLImageEditor

@objc(PhotoEditor)
class PhotoEditor: NSObject {
    var window: UIWindow?
    var bridge: RCTBridge!
    
    var resolve: RCTPromiseResolveBlock!
    var reject: RCTPromiseRejectBlock!
    
    @objc(open:withResolver:withRejecter:)
    func open(options: NSDictionary, resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock) -> Void {
        
        // handle path
        let path = options["path"] as! String
        guard let image = self.getUIImage(path: path) else {
            reject("Dont_find_image", "Couldn't find the image", nil)
            return
        }
        
        
        DispatchQueue.main.async {
            //  set config
            self.setConfiguration(options: options, resolve: resolve, reject: reject)
            self.presentController(image: image)
        }
    }
    
    private func setConfiguration(options: NSDictionary, resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock) -> Void{
        self.resolve = resolve;
        self.reject = reject;
        
        ZLImageEditorConfiguration.default().imageStickerContainerView = StickerView()
        ZLImageEditorConfiguration.default().editDoneBtnBgColor = UIColor(red:255/255.0, green:76/255.0, blue:41/255.0, alpha:1.0)
        do {
            let filters = ColorCubeLoader(bundle: .main)
            ZLImageEditorConfiguration.default().filters = try filters.load()
        } catch {
            assertionFailure("\(error)")
        }
    }
    
    private func presentController(image: UIImage) {
        if let controller = UIApplication.getTopViewController() {
            ZLEditImageViewController.showEditImageVC(parentVC:controller , image: image) { [weak self] (resImage, editModel) in
                let documentsPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0] as String
                
                let destinationPath = URL(fileURLWithPath: documentsPath).appendingPathComponent(String(Int64(Date().timeIntervalSince1970 * 1000)) + ".png")
                
                do {
                    try resImage.pngData()?.write(to: destinationPath)
                    self?.resolve(destinationPath.absoluteString)
                } catch {
                    debugPrint("writing file error", error)
                }
            }
        }
    }
    
    
    func getUIImage(path: String) -> UIImage?  {
        let uri = path.replacingOccurrences(of: "file://", with: "")
        let image: UIImage? = UIImage(contentsOfFile: uri)
        return image
    }
}

extension UIApplication {
    class func getTopViewController(base: UIViewController? = UIApplication.shared.keyWindow?.rootViewController) -> UIViewController? {
        
        if let nav = base as? UINavigationController {
            return getTopViewController(base: nav.visibleViewController)
        } else if let tab = base as? UITabBarController, let selected = tab.selectedViewController {
            return getTopViewController(base: selected)
        } else if let presented = base?.presentedViewController {
            return getTopViewController(base: presented)
        }
        
        return base
    }
}
