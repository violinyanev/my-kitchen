import SwiftUI
import SharedKit

@main
struct MyKitchenApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ContentView: View {
    @State private var recipes: [Recipe] = []
    
    var body: some View {
        NavigationView {
            VStack {
                Text("My Kitchen")
                    .font(.largeTitle)
                    .padding()
                
                Text("Recipes from Shared Module")
                    .font(.headline)
                    .padding()
                
                Button("Create Sample Recipe") {
                    createSampleRecipe()
                }
                .padding()
                
                List(recipes, id: \.id) { recipe in
                    VStack(alignment: .leading) {
                        Text(recipe.title)
                            .font(.headline)
                        Text(recipe.content)
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                    .padding(.vertical, 2)
                }
            }
            .navigationTitle("My Kitchen iOS")
        }
    }
    
    private func createSampleRecipe() {
        let newRecipe = Recipe(
            title: "iOS Sample Recipe",
            content: "This recipe was created from the iOS app using the shared Kotlin module!",
            timestamp: Int64(Date().timeIntervalSince1970 * 1000),
            id: Int64.random(in: 1...1000)
        )
        recipes.append(newRecipe)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}