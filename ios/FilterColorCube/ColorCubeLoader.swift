import CoreGraphics
import Foundation
import ImageIO
import ZLImageEditor

public enum ColorCubeLoaderError: Error {
    case failedToGetDimensionFromFilename(String)
    case failedToCreageCGDataProvider(String)
    case failedToCraeteCGImageSource(String)
}

/// An object for loading color-cube image from bundle.
/// It finds based on specified naming-rule.
///
/// `LUT_<Dimension>_<filterName>.<extension {jpg, png}>`
public final class ColorCubeLoader {

    public func load() throws -> [ZLFilter] {
        let fileManager = FileManager.default
        let fileList = fileManager.getListFileNameInBundle(bundlePath: "LUTs.bundle", parseName: true)
        
        func takeDimension(from string: String) -> Int? {
            enum Static {
                static let regex: NSRegularExpression = {
                    let pattern = "LUT_([0-9]+)_.*"
                    let regex = try! NSRegularExpression(pattern: pattern, options: [])
                    return regex
                }()
            }
            
            guard
                let matched = Static.regex.firstMatch(
                    in: string,
                    options: [],
                    range: NSRange(location: 0, length: string.count)
                )
            else {
                return nil
            }
            
            let numberString = (string as NSString).substring(with: matched.range(at: 1))
            
            return Int(numberString)
        }
        
        var filters =
        try fileList
            .filter { $0.hasPrefix("LUT_") }
            .sorted()
            .map { path -> ZLFilter in
                let fileURL = Bundle.main.resourcePath! + "/LUTs.bundle/\(path)"
                let url = URL(fileURLWithPath: fileURL)
                
                guard let dimension = takeDimension(from: path) else {
                    throw ColorCubeLoaderError.failedToGetDimensionFromFilename(path)
                }
                
                guard let dataProvider = CGDataProvider(url: url as CFURL) else {
                    throw ColorCubeLoaderError.failedToCreageCGDataProvider(path)
                }
                
                guard let imageSource = CGImageSourceCreateWithDataProvider(dataProvider, nil) else {
                    throw ColorCubeLoaderError.failedToCraeteCGImageSource(path)
                }
                
                let name = (path as NSString).deletingPathExtension
                    .replacingOccurrences(of: "LUT_\(dimension)_", with: "")
                                
                
                let colorCube = FilterColorCube.init(identifier: path, lutImage: .init(cgImageSource: imageSource), dimension: dimension)
                
                return ZLFilter(name: name) { image in
                    
                    var ci = image.ciImage
                    if ci == nil, let cg = image.cgImage {
                        ci = CIImage(cgImage: cg)
                    }
                    
                    let output = colorCube.apply(to: ci!);
                    
                    let context = CIContext()
                    guard let cgImage = context.createCGImage(output, from: output.extent) else {
                        return image
                    }
                    
                    return UIImage(cgImage: cgImage)
                }
            }
        filters.insert(ZLFilter.normal, at: 0)
        return filters
    }
}
