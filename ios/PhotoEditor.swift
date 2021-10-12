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
import ZLImageEditor

@objc(PhotoEditor)
class PhotoEditor: NSObject {
    var window: UIWindow?
    var bridge: RCTBridge!
    
    //resolve/reject assets
    var resolve: RCTPromiseResolveBlock!
    var reject: RCTPromiseRejectBlock!
    var topViewController: UIViewController!
    
    @objc(open:withResolver:withRejecter:)
    func open(options: NSDictionary, resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            if let topVC = UIApplication.getTopViewController() {
                self.topViewController = topVC
                let picker = UIImagePickerController()
                picker.delegate = self
                self.resolve = resolve
                picker.sourceType = .photoLibrary
                picker.mediaTypes = ["public.image"]
                topVC.showDetailViewController(picker, sender: self)
            }
        }
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

extension PhotoEditor: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        picker.dismiss(animated: true) {
            DispatchQueue.main.async {
                
                if let topVC = UIApplication.getTopViewController() {
                    guard let image = info[.originalImage] as? UIImage else { return }
                    ZLEditImageViewController.showEditImageVC(parentVC:topVC , image: image, editModel: .none) { [weak self] (resImage, editModel) in
                        
                        let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
                        let fileURL = documentsDirectory.appendingPathComponent("editImage\(Date()).jpg")
                        if let data = resImage.jpegData(compressionQuality: 1.0) {
                            do {
                                try data.write(to: fileURL)
                                self?.resolve(fileURL.absoluteString)
                            } catch {
                                print("error saving file to documents:", error)
                            }
                        }
                    }
                }
            }
        }
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
