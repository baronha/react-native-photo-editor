//
//  PhotoEditor.swift
//  PhotoEditor
//
//  Created by Donquijote on 27/07/2021.
//

import Foundation
import UIKit
import Photos

import Brightroom

@objc(PhotoEditor)
class PhotoEditor: NSObject {
    var window: UIWindow?
    var bridge: RCTBridge!
    
    //resolve/reject assets
    var resolve: RCTPromiseResolveBlock!
    var reject: RCTPromiseRejectBlock!
    
    @objc(open:withResolver:withRejecter:)
    func open(options: NSDictionary, resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock) -> Void {
        self.setConfiguration(options: options, resolve: resolve, reject: reject)
        ColorCubeStorage.loadToDefault()
        let path = options["path"] as! String
        let image = getUIImage(path: path)
        //check exist image in local
        if(image == nil){
            reject("Dont_find_image", "Couldn't find the image", nil)
            return;
        }
        
        // Creating view controller
        let stack = EditingStack(
            imageProvider: .init(image: image!)
        )
        self.present(stack)
    }
    
    private func setConfiguration(options: NSDictionary, resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock) -> Void{
        self.resolve = resolve;
        self.reject = reject;
    }
    
    private func present(_ editingStack: EditingStack) {
        DispatchQueue.main.async {
            var options = ClassicImageEditOptions()
            options.croppingAspectRatio = .none
            //      options.classes.control.rootControl = Control.self
            let controller = ClassicImageEditViewController(editingStack: editingStack, options: options)
            
            controller.handlers.didEndEditing = { [weak self] controller, stack in
                
                guard self != nil else { return }
                controller.dismiss(animated: true, completion: nil)
                
                try! stack.makeRenderer().render { result in
                    switch result {
                    case let .success(rendered):
                        let documentsPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0] as String
                        
                        let destinationPath = URL(fileURLWithPath: documentsPath).appendingPathComponent(String(Int64(Date().timeIntervalSince1970 * 1000)) + ".png")
                        
                        do {
                            try rendered.uiImage.pngData()?.write(to: destinationPath)
                            self?.resolve(destinationPath.absoluteString)
                        } catch {
                            debugPrint("writing file error", error)
                        }
                        
                        break;
                    case let .failure(error):
                        print(error)
                    }
                }
            }
            
            controller.handlers.didCancelEditing = { controller in
                controller.dismiss(animated: true, completion: nil)
                self.reject("User_Cancelled", "User canceled editing", nil)
            }
            
            let navigationController = UINavigationController(rootViewController: controller)
            
            navigationController.modalPresentationStyle = .fullScreen
            
            self.getTopMostViewController()?.present(navigationController, animated: true, completion: nil)
        }
    }
    
    func getUIImage(path: String) -> UIImage?  {
        let uri = path.replacingOccurrences(of: "file://", with: "")
        let image: UIImage? = UIImage(contentsOfFile: uri)
        return image
    }
    
    func getTopMostViewController() -> UIViewController? {
        var topMostViewController = UIApplication.shared.keyWindow?.rootViewController
        while let presentedViewController = topMostViewController?.presentedViewController {
            topMostViewController = presentedViewController
        }
        return topMostViewController
    }
    
}

extension ColorCubeStorage {
    static func loadToDefault() {
        do {
            let loader = ColorCubeLoader(bundle: .main)
            self.default.filters = try loader.load()
            
        } catch {
            assertionFailure("\(error)")
        }
    }
}
