import SwiftUI
import Shared

struct ContentView: View {
    @State private var showContent = false
    var body: some View {
        VStack {
            VStack(spacing: 16) {
                Image(systemName: "swift")
                    .font(.system(size: 80))
                    .foregroundColor(.accentColor)
                Text("Kvs Storage")
            }
            Button("Click me!") {
             
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
    }
    
    func hello() async  {
        let kvs = Storage.shared.kvs(name: "")
        try! await kvs.edit().putBoolean(key: "", value: true).commit()
        
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
