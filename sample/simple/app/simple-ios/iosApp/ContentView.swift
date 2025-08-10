import UIKit
import SwiftUI
import simple

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return ControllerKt.createViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
