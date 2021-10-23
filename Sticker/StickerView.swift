//
//  ImageStickerContainerView.swift
//  Example
//
//  Created by long on 2020/11/20.
//

import UIKit
import ZLImageEditor

class StickerView: UIView, ZLImageStickerContainerDelegate {
    
    static let baseViewH: CGFloat = 500
    var baseView: UIView!
    var collectionView: UICollectionView!
    var selectImageBlock: ((UIImage) -> Void)?
    var hideBlock: (() -> Void)?
    var datas : [String] =  []
    
    public init(stickers: [String]) {
        super.init(frame: .zero)
        self.setupUI()
        self.datas = stickers
        self.setupData()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let path = UIBezierPath(roundedRect: CGRect(x: 0, y: 0, width: self.frame.width, height: StickerView.baseViewH), byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 8, height: 8))
        self.baseView.layer.mask = nil
        let maskLayer = CAShapeLayer()
        maskLayer.path = path.cgPath
        //  gesture
        let gesture = UIPanGestureRecognizer.init(target: self, action: #selector(hideBtnClick))
        gesture.delegate = self
        self.baseView.addGestureRecognizer(gesture)
        
        self.baseView.layer.mask = maskLayer
    }
    
    func gesture(recognizer: UIPanGestureRecognizer) {
        let translation = recognizer.translation(in: self.baseView)
        let y = self.baseView.frame.minY
        self.baseView.frame = CGRect(x: 0, y: y + translation.y, width: baseView.frame.width, height: baseView.frame.height)
        recognizer.setTranslation(CGPoint.zero, in: self.baseView)
    }
    
    private func setupData(){
        let fm = FileManager.default
        datas = datas + fm.getListFileNameInBundle(bundlePath: "Stickers.bundle") 
    }
    
    func setupUI() {
        self.baseView = UIView()
        self.addSubview(self.baseView)
        self.baseView.snp.makeConstraints { (make) in
            make.left.right.equalTo(self)
            make.bottom.equalTo(self.snp.bottom).offset(StickerView.baseViewH)
            make.height.equalTo(StickerView.baseViewH)
        }
        
        let visualView = UIVisualEffectView(effect: UIBlurEffect(style: .dark))
        self.baseView.addSubview(visualView)
        visualView.snp.makeConstraints { (make) in
            make.edges.equalTo(self.baseView)
        }
        
        let toolView = UIView()
        toolView.backgroundColor = UIColor(white: 0.4, alpha: 0.4)
        self.baseView.addSubview(toolView)
        toolView.snp.makeConstraints { (make) in
            make.top.left.right.equalTo(self.baseView)
            make.height.equalTo(42)
        }
        
        let hideBtn = UIButton(type: .custom)
        //        hideBtn.setImage(UIImage(named: "close"), for: .normal)
        hideBtn.backgroundColor = .clear
        
        hideBtn.titleLabel?.text = "Close"
        hideBtn.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        hideBtn.titleLabel?.textColor = UIColor.white
        
        hideBtn.addTarget(self, action: #selector(hideBtnClick), for: .touchUpInside)
        
        toolView.addSubview(hideBtn)
        hideBtn.snp.makeConstraints { (make) in
            make.centerY.equalTo(toolView)
            make.right.equalTo(toolView).offset(-20)
            make.size.equalTo(CGSize(width: 40, height: 40))
        }
        
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .vertical
        layout.sectionInset = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
        layout.minimumLineSpacing = 5
        layout.minimumInteritemSpacing = 5
        
        self.collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        self.collectionView.backgroundColor = .clear
        self.collectionView.delegate = self
        self.collectionView.dataSource = self
        self.baseView.addSubview(self.collectionView)
        self.collectionView.snp.makeConstraints { (make) in
            make.top.equalTo(toolView.snp.bottom)
            make.left.right.bottom.equalTo(self.baseView)
        }
        
        self.collectionView.register(StickerCell.self, forCellWithReuseIdentifier: NSStringFromClass(StickerCell.classForCoder()))
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(hideBtnClick))
        tap.delegate = self
        self.addGestureRecognizer(tap)
    }
    
    @objc func hideBtnClick() {
        self.hide()
    }
    
    func show(in view: UIView) {
        if self.superview !== view {
            self.removeFromSuperview()
            
            view.addSubview(self)
            self.snp.makeConstraints { (make) in
                make.edges.equalTo(view)
            }
            view.layoutIfNeeded()
        }
        
        self.isHidden = false
        UIView.animate(withDuration: 0.3) {
            self.baseView.snp.updateConstraints { (make) in
                make.bottom.equalTo(self.snp.bottom)
            }
            view.layoutIfNeeded()
        }
    }
    
    func hide() {
        self.hideBlock?()
        
        UIView.animate(withDuration: 0.3) {
            self.baseView.snp.updateConstraints { (make) in
                make.bottom.equalTo(self.snp.bottom).offset(StickerView.baseViewH)
            }
            self.superview?.layoutIfNeeded()
        } completion: { (_) in
            self.isHidden = true
        }
        
    }
    
}


extension StickerView: UIGestureRecognizerDelegate {
    
    public override func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        let location = gestureRecognizer.location(in: self)
        return !self.baseView.frame.contains(location)
    }
}


extension StickerView: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        let column: CGFloat = 5
        let spacing: CGFloat = 20 + 5 * (column - 1)
        let w = (collectionView.frame.width - spacing) / column
        return CGSize(width: w, height: w)
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.datas.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(StickerCell.classForCoder()), for: indexPath) as! StickerCell
        
        let item = self.datas[indexPath.row]
        
        handleImageInCell(from: item) { image in
            cell.imageView.image = image
        }
        
        
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let item = self.datas[indexPath.row]
        handleImageInCell(from: item) { image in
            self.selectImageBlock?(image)
            self.hide()
        }
    }
    
    func handleImageInCell(from urlString: String, completion: @escaping (UIImage) -> ()) {
        if(urlString.contains("http")){
            let url = URL(string: urlString)
            getData(from: url!) { data, response, error in
                guard let data = data, error == nil else { return }
                // always update the UI from the main thread
                DispatchQueue.main.async() {
                    let image = UIImage(data: data)
                    completion(image!)
                }
            }
        }else{
            completion(UIImage(named: urlString)!)
        }
    }
    
    func getData(from url: URL, completion: @escaping (Data?, URLResponse?, Error?) -> ()) {
        URLSession.shared.dataTask(with: url, completionHandler: completion).resume()
    }
    
    //    func numberOfSections(in collectionView: UICollectionView) -> Int {
    //        return 2
    //    }
    
}

extension FileManager {
    func getListFileNameInBundle(bundlePath: String) -> [String] {
        
        let fileManager = FileManager.default
        let bundleURL = Bundle.main.bundleURL
        let assetURL = bundleURL.appendingPathComponent(bundlePath)
        do {
            let contents = try fileManager.contentsOfDirectory(at: assetURL, includingPropertiesForKeys: [URLResourceKey.nameKey, URLResourceKey.isDirectoryKey], options: .skipsHiddenFiles)
            return contents.map{$0.lastPathComponent}
        }
        catch {
            return []
        }
    }
    
    func getImageInBundle(bundlePath: String) -> UIImage? {
        let bundleURL = Bundle.main.bundleURL
        let assetURL = bundleURL.appendingPathComponent(bundlePath)
        return UIImage.init(contentsOfFile: assetURL.relativePath)
    }
}
