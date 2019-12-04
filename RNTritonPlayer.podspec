
Pod::Spec.new do |s|
  s.name         = "RNTritonPlayer"
  s.version      = "1.0.0"
  s.summary      = "RNTritonPlayer"
  s.description  = <<-DESC
                  Triton Player library for React-Native
                   DESC
  s.homepage     = "https://github.com/Brianvdb/react-native-triton"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNTritonPlayer.git", :branch => "master" }
  s.source_files   = 'ios/**/*.{h,m}'
  s.exclude_files  = 'android/**/*'
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  